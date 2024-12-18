const path = require("path");
const os = require("os");
const dist = path.resolve("../../../js/node_modules/sql.js/dist/")
const wasm = path.join(dist, "sql-wasm.wasm")

config.files.push({
    pattern: wasm,
    served: true,
    watched: false,
    included: false,
    nocache: false,
});

config.proxies["/sql-wasm.wasm"] = path.join("/absolute/", wasm)

// Adapted from: https://github.com/ryanclark/karma-webpack/issues/498#issuecomment-790040818
const output = {
  path: path.join(os.tmpdir(), '_karma_webpack_') + Math.floor(Math.random() * 1000000),
}
config.set({
  webpack: {...config.webpack, output},
  client: {
      mocha: {
        timeout: 20000
      }
    }
});

config.files.push({
  pattern: `${output.path}/**/*`,
  watched: false,
  included: false,
});

config.files.push({
   pattern: 'kotlin/*.json',
   watched: true,
   served: true,
   included: false
});