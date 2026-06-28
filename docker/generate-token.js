'use strict';
// Generates signed JWTs for local SQL backend testing.
// No npm packages -- uses Node.js built-in crypto only.
// Usage: node generate-token.js
// Reads XFORM_JWT_SECRET (base64-encoded) from environment.

const crypto = require('crypto');

const secret = process.env.XFORM_JWT_SECRET;
if (!secret) {
    console.error('ERROR: XFORM_JWT_SECRET is not set.');
    console.error('Set it to a base64-encoded random key, e.g.:');
    console.error('  export XFORM_JWT_SECRET=$(openssl rand -base64 32)');
    process.exit(1);
}

let secretBytes;
try {
    secretBytes = Buffer.from(secret, 'base64');
    if (secretBytes.length < 32) throw new Error('Key too short');
} catch (e) {
    console.error('ERROR: XFORM_JWT_SECRET must be a base64-encoded value of at least 32 bytes.');
    process.exit(1);
}

const now = Math.floor(Date.now() / 1000);
const oneHour = 3600;

function makeToken(subject, roles) {
    const header = { alg: 'HS256', typ: 'JWT' };
    const payload = {
        sub: subject,
        iat: now,
        exp: now + oneHour,
        jti: crypto.randomUUID(),
        roles: roles
    };
    const enc = obj => Buffer.from(JSON.stringify(obj))
        .toString('base64url');
    const sigInput = enc(header) + '.' + enc(payload);
    const sig = crypto.createHmac('sha256', secretBytes)
        .update(sigInput).digest('base64url');
    return sigInput + '.' + sig;
}

const senderToken = makeToken('sql-test-sender', ['xform-sender']);
const adminToken  = makeToken('sql-test-admin',  ['xform-sender', 'admin']);

console.log('');
console.log('=== SQL Backend Test Tokens (valid 1 hour) ===');
console.log('');
console.log('-- Sender token (for patient queries and bulk export):');
console.log(senderToken);
console.log('');
console.log('-- Admin token (sender + admin roles):');
console.log(adminToken);
console.log('');
console.log('Usage:  curl -k -H "Authorization: Bearer <token>" https://localhost:444/sql/fhir/test/Patient?...');
console.log('');
