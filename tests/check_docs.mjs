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
