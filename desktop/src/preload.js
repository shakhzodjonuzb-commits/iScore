const { contextBridge } = require('electron');
const crypto = require('crypto');
const fs = require('fs');
const path = require('path');

let SECRET = 'HXDMVJECJJWSRB3HWIZR4IFUGFTMXBOZ';
try {
  const cfg = JSON.parse(fs.readFileSync(path.join(__dirname, '..', 'config.json'), 'utf8'));
  if (cfg.totpSecret) SECRET = cfg.totpSecret;
} catch (e) { /* default */ }

const ALPHABET = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ234567';
function base32Decode(str) {
  let bits = 0, value = 0;
  const out = [];
  for (const ch of str.toUpperCase()) {
    const v = ALPHABET.indexOf(ch);
    if (v < 0) continue;
    value = (value << 5) | v;
    bits += 5;
    if (bits >= 8) {
      bits -= 8;
      out.push((value >>> bits) & 0xff);
      value &= (1 << bits) - 1;
    }
  }
  return Buffer.from(out);
}

function codeAt(counter) {
  const msg = Buffer.alloc(8);
  msg.writeBigUInt64BE(BigInt(counter));
  const h = crypto.createHmac('sha1', base32Decode(SECRET)).update(msg).digest();
  const off = h[h.length - 1] & 0x0f;
  const bin = ((h[off] & 0x7f) << 24) | (h[off + 1] << 16) | (h[off + 2] << 8) | h[off + 3];
  return String(bin % 1000000).padStart(6, '0');
}

function verify(input) {
  if (!/^\d{6}$/.test(input)) return false;
  const counter = Math.floor(Date.now() / 30000);
  let ok = false;
  for (let d = -1; d <= 1; d++) {
    const exp = Buffer.from(codeAt(counter + d));
    const got = Buffer.from(input);
    if (crypto.timingSafeEqual(exp, got)) ok = true;
  }
  return ok;
}

contextBridge.exposeInMainWorld('totp', { verify });
