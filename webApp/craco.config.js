// const CopyWebpackPlugin = require('copy-webpack-plugin');
const path = require('path');
const PROD = process.env.NODE_ENV === 'production';
const CopyWebpackPlugin = require('copy-webpack-plugin');
const {addBeforeLoader, loaderByName} = require('@craco/craco');

module.exports = {
    webpack: {
        module: {
            rules: [
                // wasm files should not be processed but just be emitted and we want
                // to have their public URL.
                {
                    test: /\.wasm$/,
                    type: "javascript/auto",
                    loader: "file-loader",
                }
            ]
        },
        configure: webpackConfig => {
            webpackConfig.resolve.fallback = {
                fs: false,
                path: false,
                crypto: false,
            }
            // const wasmExtensionRegExp = /\.wasm$/;
            // webpackConfig.resolve.extensions.push('.wasm');
            //
            // webpackConfig.module.rules.forEach((rule) => {
            //     (rule.oneOf || []).forEach((oneOf) => {
            //         if (oneOf.loader) {
            //
            //             console.log(oneOf.loader.indexOf('file-loader') >= 0)
            //         }
            //         if (oneOf.loader && oneOf.loader.indexOf('file-loader') >= 0) {
            //             oneOf.exclude.push(wasmExtensionRegExp);
            //         }
            //     });
            // });

            // const wasmLoader = {
            //     // ident: /\.wasm$/,
            //     // exclude: /node_modules/,
            //     loader: 'wasm-loader',
            // };

            // addBeforeLoader(webpackConfig, loaderByName('file-loader'), wasmLoader);

            // const wasmExtensionRegExp = /\.wasm$/
            // webpackConfig.resolve.extensions.push('.wasm')
            // webpackConfig.experiments = {
            //     syncWebAssembly: true,
            // }
            //  console.log(webpackConfig.module.rules)
            // webpackConfig.module.rules.forEach(rule => {
            //     ;(rule.oneOf || []).forEach(oneOf => {
            //         if (oneOf.type === 'asset/resource') {
            //             oneOf.exclude.push(wasmExtensionRegExp)
            //         }
            //     })
            // })
            return webpackConfig;
        },
        plugins: [
            new CopyWebpackPlugin({
                    patterns: [
                        {
                            from: PROD
                                ? './node_modules/sql.js/dist/sql-wasm.wasm'
                                : './node_modules/sql.js/dist/sql-wasm-debug.wasm',
                            to: './static/js/'
                        }
                    ]
                }
            ),
        ],
    },
};