/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
export function lighten(color, percent) {
    let {r, g, b, a} = readColor(color);
    return writeColor({
        r: lightenValue(r, percent),
        g: lightenValue(g, percent),
        b: lightenValue(b, percent),
        a: a,
    })
}

export function darken(color, percent) {
    let {r, g, b, a} = readColor(color);
    return writeColor({
        r: darkenValue(r, percent),
        g: darkenValue(g, percent),
        b: darkenValue(b, percent),
        a: a,
    })
}

function lightenValue(value, percent) {
    return Math.round(value - value * percent);
}

function darkenValue(value, percent) {
    return Math.round(value + (255 - value) * percent);
}

export function readColor(color) {
    let rgb = color.replace(/\s*/g, '').match(/rgb\((\d*)\,(\d*)\,(\d*)\)/)
    if (rgb != null) {
        return {
            r: parseInt(rgb[1]),
            g: parseInt(rgb[2]),
            b: parseInt(rgb[3]),
            a: 1,
        }
    }
    let rgba = color.replace(/\s*/g, '').match(/rgba\((\d*)\,(\d*)\,(\d*)\,(.*)\)/)
    if (rgba != null) {
        return {
            r: parseInt(rgba[1]),
            g: parseInt(rgba[2]),
            b: parseInt(rgba[3]),
            a: parseFloat(rgba[4]),
        }
    }
    return null
}

function writeColor(color) {
    return `rgba(${color.r},${color.g},${color.b},${color.a})`;
}
