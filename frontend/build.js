const fs = require('fs');
const path = require('path');

const src = path.join(__dirname, 'src', 'index.html');
const outDir = path.join(__dirname, 'dist');
const out = path.join(outDir, 'index.html');

fs.mkdirSync(outDir, { recursive: true });
fs.copyFileSync(src, out);
console.log('Built frontend artifact at dist/index.html');
