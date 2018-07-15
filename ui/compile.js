const path = require('path');
const fs = require('fs');
const solc  = require('solc');

const brPath = path.resolve(__dirname, 'contracts', 'BlockchainRoyale.sol');
const source = fs.readFileSync(brPath, 'utf8');


module.exports = solc.compile(source, 1).contracts[':BlockchainRoyale'];

