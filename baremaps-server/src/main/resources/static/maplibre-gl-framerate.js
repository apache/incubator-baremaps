/**
 * Copyright (c) 2019, Mapbox
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 *
 * Source:
 * https://github.com/mapbox/mapbox-gl-framerate
 */
(function (global, factory) {
    typeof exports === 'object' && typeof module !== 'undefined' ? module.exports = factory() :
        typeof define === 'function' && define.amd ? define(factory) :
            (global.FrameRateControl = factory());
}(this, (function () { 'use strict';

    function _classCallCheck(instance, Constructor) {
        if (!(instance instanceof Constructor)) {
            throw new TypeError("Cannot call a class as a function");
        }
    }

    function _defineProperty(obj, key, value) {
        if (key in obj) {
            Object.defineProperty(obj, key, {
                value: value,
                enumerable: true,
                configurable: true,
                writable: true
            });
        } else {
            obj[key] = value;
        }

        return obj;
    }

    function _objectSpread(target) {
        for (var i = 1; i < arguments.length; i++) {
            var source = arguments[i] != null ? arguments[i] : {};
            var ownKeys = Object.keys(source);

            if (typeof Object.getOwnPropertySymbols === 'function') {
                ownKeys = ownKeys.concat(Object.getOwnPropertySymbols(source).filter(function (sym) {
                    return Object.getOwnPropertyDescriptor(source, sym).enumerable;
                }));
            }

            ownKeys.forEach(function (key) {
                _defineProperty(target, key, source[key]);
            });
        }

        return target;
    }

    var FrameRateControl = function FrameRateControl(options) {
        var _this = this;

        _classCallCheck(this, FrameRateControl);

        _defineProperty(this, "onAdd", function (map) {
            _this.map = map;
            var dpr = window.devicePixelRatio;
            var _this$options = _this.options,
                width = _this$options.width,
                graphWidth = _this$options.graphWidth,
                graphHeight = _this$options.graphHeight,
                color = _this$options.color,
                background = _this$options.background,
                font = _this$options.font;
            var el = _this.container = document.createElement('div');
            el.className = 'maplibregl-ctrl maplibregl-ctrl-fps';
            el.style.backgroundColor = background;
            el.style.borderRadius = '6px';
            _this.readOutput = document.createElement('div');
            _this.readOutput.style.color = color;
            _this.readOutput.style.fontFamily = font;
            _this.readOutput.style.padding = '0 5px 5px';
            _this.readOutput.style.fontSize = '9px';
            _this.readOutput.style.fontWeight = 'bold';
            _this.readOutput.textContent = 'Waiting…';
            _this.canvas = document.createElement('canvas');
            _this.canvas.className = 'maplibregl-ctrl-canvas';
            _this.canvas.width = width;
            _this.canvas.height = graphHeight;
            _this.canvas.style.cssText = "width: ".concat(width / dpr, "px; height: ").concat(graphHeight / dpr, "px;");
            el.appendChild(_this.readOutput);
            el.appendChild(_this.canvas);

            _this.map.on('movestart', _this.onMoveStart);

            _this.map.on('moveend', _this.onMoveEnd);

            return _this.container;
        });

        _defineProperty(this, "onMoveStart", function () {
            _this.frames = 0;
            _this.time = performance.now();

            _this.map.on('render', _this.onRender);
        });

        _defineProperty(this, "onMoveEnd", function () {
            var now = performance.now();

            _this.updateGraph(_this.getFPS(now));

            _this.frames = 0;
            _this.time = null;

            _this.map.off('render', _this.onRender);
        });

        _defineProperty(this, "onRender", function () {
            _this.frames++;
            var now = performance.now();

            if (now >= _this.time + 1e3) {
                _this.updateGraph(_this.getFPS(now));

                _this.frames = 0;
                _this.time = now;
            }
        });

        _defineProperty(this, "getFPS", function (now) {
            _this.totalTime += now - _this.time, _this.totalFrames += _this.frames;
            return Math.round(1e3 * _this.frames / (now - _this.time)) || 0;
        });

        _defineProperty(this, "updateGraph", function (fpsNow) {
            var _this$options2 = _this.options,
                barWidth = _this$options2.barWidth,
                graphRight = _this$options2.graphRight,
                graphTop = _this$options2.graphTop,
                graphWidth = _this$options2.graphWidth,
                graphHeight = _this$options2.graphHeight,
                background = _this$options2.background,
                color = _this$options2.color;

            var context = _this.canvas.getContext('2d');

            var fps = Math.round(1e3 * _this.totalFrames / _this.totalTime) || 0;
            var rect = (barWidth);
            context.fillStyle = background;
            context.globalAlpha = 1;
            context.fillRect(0, 0, graphWidth, graphTop);
            context.fillStyle = color;
            _this.readOutput.textContent = "".concat(fpsNow, " FPS (").concat(fps, " Avg)");
            context.drawImage(_this.canvas, graphRight + rect, graphTop, graphWidth - rect, graphHeight, graphRight, graphTop, graphWidth - rect, graphHeight);
            context.fillRect(graphRight + graphWidth - rect, graphTop, rect, graphHeight);
            context.fillStyle = background;
            context.globalAlpha = 0.75;
            context.fillRect(graphRight + graphWidth - rect, graphTop, rect, (1 - fpsNow / 100) * graphHeight);
        });

        _defineProperty(this, "onRemove", function () {
            _this.map.off('render', _this.onRender);

            _this.map.off('movestart', _this.onMoveStart);

            _this.map.off('moveend', _this.onMoveEnd);

            _this.container.parentNode.removeChild(_this.container);

            _this.map = null;
            return _this;
        });

        var _dpr = window.devicePixelRatio;
        var defaultOptions = {
            background: 'rgba(0,0,0,0.9)',
            barWidth: 4 * _dpr,
            color: '#7cf859',
            font: 'Monaco, Consolas, Courier, monospace',
            graphHeight: 60 * _dpr,
            graphWidth: 90 * _dpr,
            graphTop: 0,
            graphRight: 5 * _dpr,
            width: 100 * _dpr
        };
        this.frames = 0;
        this.totalTime = 0;
        this.totalFrames = 0;
        this.options = _objectSpread({}, options, defaultOptions);
    };

    if (window.maplibregl) {
        maplibregl.FrameRateControl = FrameRateControl;
    }

    return FrameRateControl;

})));