// Проверки документации скилла, без сети. Запуск из корня репозитория: node tests/check_docs.mjs
// 1. Шаблоны из раздела «Шаблоны» в toolchain.md совпадают с файлами, которые собирает CI.
//    Workflow репозитория — это шаблон ci.yml плюс строки с пометкой «# skill-ci».
// 2. Относительные ссылки и якоря между Markdown-файлами ведут на существующие файлы и заголовки
//    (якоря по правилам GitHub). В таблицах нет голых URL.
import fs from 'node:fs';
import path from 'node:path';

const root = process.cwd();
const read = (p) => fs.readFileSync(path.join(root, p), 'utf8').replace(/\r/g, '');
const norm = (t) => t.split('\n').map((l) => l.trimEnd()).join('\n').trim();
let problems = 0;
const fail = (msg) => {
  console.log(`FAIL ${msg}`);
  problems++;
};

// ---- 1. шаблоны ----
const templates = [
  ['xml', 'tests/templates/maven/pom.xml'],
  ['text', 'tests/templates/maven/.mvn/jvm.config'],
  ['kotlin', 'tests/templates/gradle/settings.gradle.kts'],
  ['toml', 'tests/templates/gradle/gradle/libs.versions.toml'],
  ['kotlin', 'tests/templates/gradle/build.gradle.kts'],
  ['properties', 'tests/templates/gradle/gradle.properties'],
  ['yaml', '.github/workflows/ci.yml'],
];
const toolchain = read('java-knowledge/references/toolchain.md').split('\n');
const start = toolchain.findIndex((l) => l.startsWith('## Шаблоны'));
const blocks = [];
for (let i = start, cur = null; start >= 0 && i < toolchain.length; i++) {
  const l = toolchain[i];
  if (l.startsWith('```')) {
    if (cur === null) cur = { lang: l.slice(3).trim(), lines: [] };
    else {
      blocks.push(cur);
      cur = null;
    }
  } else if (cur !== null) cur.lines.push(l);
}
if (blocks.length !== templates.length) {
  fail(`toolchain.md «Шаблоны»: ${blocks.length} code blocks, expected ${templates.length}`);
}
templates.forEach(([lang, file], i) => {
  const block = blocks[i];
  if (!block) return;
  let actual = read(file);
  if (file.endsWith('ci.yml')) {
    actual = actual.split('\n').filter((l) => !l.includes('# skill-ci')).join('\n');
  }
  if (block.lang !== lang) fail(`block ${i + 1}: language ${block.lang}, expected ${lang} for ${file}`);
  if (norm(block.lines.join('\n')) !== norm(actual)) fail(`toolchain.md template block ${i + 1} differs from ${file}`);
  else console.log(`ok   template ${file}`);
});

// Фрагмент pom.xml для JMH из performance.md должен стоять в tests/jmh/pom.xml дословно.
{
  const perf = read('java-knowledge/references/performance.md').split('\n');
  const at = perf.findIndex((l) => l.includes('annotationProcessorPaths') && l.includes('jmh-generator-annprocess'));
  const open = perf.findIndex((l, i) => i > at && l === '```xml');
  const close = perf.findIndex((l, i) => i > open && l === '```');
  const fragment = norm(perf.slice(open + 1, close).join('\n'));
  if (at < 0 || open < 0 || close < 0) fail('performance.md: JMH pom fragment not found');
  else if (!norm(read('tests/jmh/pom.xml')).includes(fragment)) fail('performance.md JMH pom fragment differs from tests/jmh/pom.xml');
  else console.log('ok   template tests/jmh/pom.xml (fragment from performance.md)');
}

// Блоки с пометкой <!-- template: путь --> на строке перед ограждением кода должны совпадать с файлом.
{
  const refsDir = path.join(root, 'java-knowledge', 'references');
  let marked = 0;
  for (const name of fs.readdirSync(refsDir).filter((n) => n.endsWith('.md'))) {
    const ls = fs.readFileSync(path.join(refsDir, name), 'utf8').replace(/\r/g, '').split('\n');
    ls.forEach((l, i) => {
      const m = /^<!-- template: (\S+) -->$/.exec(l.trim());
      if (!m) return;
      marked++;
      const where = `${name}:${i + 1}`;
      if (!ls[i + 1]?.startsWith('```')) return fail(`${where} template marker is not followed by a code fence`);
      const end = ls.findIndex((x, j) => j > i + 1 && x.startsWith('```'));
      if (end < 0) return fail(`${where} unterminated code block`);
      const file = path.join(root, m[1]);
      if (!fs.existsSync(file)) return fail(`${where} template file ${m[1]} does not exist`);
      if (norm(ls.slice(i + 2, end).join('\n')) !== norm(read(m[1]))) fail(`${where} block differs from ${m[1]}`);
    });
  }
  console.log(`ok   ${marked} marked templates compared with tests/ files`);
}

// ---- 2. ссылки и якоря ----
const mdFiles = [];
(function walk(dir) {
  for (const e of fs.readdirSync(dir, { withFileTypes: true })) {
    const p = path.join(dir, e.name);
    if (e.isDirectory()) walk(p);
    else if (p.endsWith('.md')) mdFiles.push(p);
  }
})(path.join(root, 'java-knowledge'));
mdFiles.push(path.join(root, 'README.md'));

const slug = (h) => h.trim().toLowerCase().replace(/[^\p{L}\p{N}\- _]/gu, '').replace(/ /g, '-');
const lines = new Map(mdFiles.map((f) => [f, fs.readFileSync(f, 'utf8').replace(/\r/g, '').split('\n')]));
const anchors = new Map();
for (const [f, ls] of lines) {
  const set = new Set();
  const seen = new Map();
  let code = false;
  for (const l of ls) {
    if (/^\s*```/.test(l)) code = !code;
    const m = !code && /^#{1,6} (.*)$/.exec(l);
    if (!m) continue;
    let s = slug(m[1].replace(/\[([^\]]*)\]\([^)]*\)/g, '$1'));
    const n = seen.get(s) ?? 0;
    seen.set(s, n + 1);
    if (n) s = `${s}-${n}`;
    set.add(s);
  }
  anchors.set(f, set);
}
let links = 0;
for (const [f, ls] of lines) {
  let code = false;
  ls.forEach((l, i) => {
    if (/^\s*```/.test(l)) code = !code;
    if (code) return;
    const where = `${path.relative(root, f)}:${i + 1}`;
    if (/^\|\s*<?https?:\/\//.test(l)) fail(`${where} bare URL in table`);
    for (const m of l.matchAll(/\]\(([^)\s]+)\)/g)) {
      const target = m[1];
      if (/^(https?:|mailto:)/.test(target)) continue;
      links++;
      const [p, a] = target.split('#');
      const file = p ? path.resolve(path.dirname(f), decodeURI(p)) : f;
      if (!fs.existsSync(file)) fail(`${where} missing file ${target}`);
      else if (a !== undefined && file.endsWith('.md') && !anchors.get(file)?.has(decodeURIComponent(a))) {
        fail(`${where} missing anchor ${target}`);
      }
    }
  });
}
console.log(`ok   ${links} relative links in ${mdFiles.length} files checked`);

if (problems) {
  console.log(`${problems} problem(s)`);
  process.exit(1);
}
console.log('all checks passed');
