/**
 Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 in compliance with the License. You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software distributed under the License
 is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 or implied. See the License for the specific language governing permissions and limitations under
 the License.
 **/
export function withSortKeys(directives) {
    return directives
        .map(withFillSortKey)
        .map(withLineSortKey);
}

export function withFillSortKey(directive, index, array) {
    return directive['fill-color'] ?{
        ...directive,
        'fill-sort-key': array.length - index,
    } : directive;
}

export function withLineSortKey(directive, index, array) {
    return directive['line-width'] ? {
        ...directive,
        'line-sort-key': array.length - index,
    } : directive;
}

export function withSymbolSortKeys(directives) {
    return directives.map(withSymbolSortKey);
}

export function withSymbolSortKey(directive, index) {
    return directive['symbol-sort-key'] ? directive : {
        ...directive,
        'symbol-sort-key': index,
    };
}

export function asLayerObject(directives = [], baseLayer = {}) {
    return {
        ...baseLayer,
        filter: asFilterProperty(directives, baseLayer['filter']),
        layout: asLayoutProperty(directives, baseLayer['layout']),
        paint: asPaintProperty(directives, baseLayer['paint']),
    };
}

export function asLayoutProperty(directives = [], baseLayout = {}) {
    return Object.assign(
        {
            ...textFont(directives),
            ...textField(directives),
            ...textSize(directives),
            ...textMaxWidth(directives),
            ...iconImage(directives),
            ...lineSortKey(directives),
            ...fillSortKey(directives),
            ...symbolSortKey(directives),
            ...labelSize(directives),
        },
        baseLayout,
    )
}

export function asPaintProperty(directives = [], basePaint = {}) {
    return Object.assign(
        {
            ...textColor(directives),
            ...textHaloColor(directives),
            ...textHaloWidth(directives),
            ...iconColor(directives),
            ...fillColor(directives),
            ...fillOutlineColor(directives),
            ...lineColor(directives),
            ...lineWidth(directives),
            ...lineGapWidth(directives),
            ...roadWidth(directives),
            ...roadGapWidth(directives),
            ...labelColor(directives),
        },
        basePaint,
    )
}

export function asFilterProperty(directives = [], filter = []) {
    if (directives.length > 0 && filter.length > 0) {
        return [
            'all',
            filter,
            ['any', ...directives.map((rule) => rule['filter'])],
        ];
    } else if (directives.length > 0) {
        return ['any', ...directives.map((rule) => rule['filter'])];
    } else if (filter.length > 0) {
        return filter;
    } else {
        return [];
    }
}

function iconImage(directives) {
    return mergeDirectives(directives, 'icon-image', 'none')
}

function iconColor(directives) {
    return mergeDirectives(directives, 'icon-color', 'rgba(0, 0, 0, 0)')
}

function textFont(directives) {
    return mergeDirectives(directives, 'text-font', "Arial")
}

function textField(directives) {
    return mergeDirectives(directives, 'text-field', null)
}

function textSize(directives) {
    return mergeDirectives(directives, 'text-size', 12)
}

function textMaxWidth(directives) {
    return mergeDirectives(directives, 'text-max-width', 4)
}

function textColor(directives) {
    return mergeDirectives(directives, 'text-color', 'rgba(0, 0, 0, 0)')
}

function textHaloColor(directives) {
    return mergeDirectives(directives, 'text-halo-color', 'rgba(0, 0, 0, 0)')
}

function textHaloWidth(directives) {
    return mergeDirectives(directives, 'text-halo-width', 0)
}

function fillColor(directives) {
    return mergeDirectives(directives, 'fill-color', 'rgba(0, 0, 0, 0)')
}

function fillOutlineColor(directives) {
    return mergeDirectives(directives, 'fill-outline-color', 'rgba(0, 0, 0, 0)')
}

function lineColor(directives) {
    return mergeDirectives(directives, 'line-color', 'rgba(0, 0, 0, 0)')
}

function lineWidth(directives) {
    return mergeDirectives(directives, 'line-width', 0)
}

function lineGapWidth(directives) {
    return mergeDirectives(directives, 'line-gap-width', 0)
}

function lineSortKey(directives) {
    return mergeDirectives(directives, 'line-sort-key', 0)
}

function fillSortKey(directives) {
    return mergeDirectives(directives, 'fill-sort-key', 0)
}

function symbolSortKey(directives) {
    return mergeDirectives(directives, 'symbol-sort-key', 0)
}

function mergeDirectives(directives, property, value) {
    let cases = directives.flatMap((rule) => {
        if (rule[property]) {
            return [rule['filter'], rule[property]]
        } else {
            return []
        }
    })
    if (cases.length == 0) {
        return {}
    }
    return {
        [property]: ['case', ...cases, value],
    }
}

function roadWidth(directives) {
    return mergeInterpolatedDirective(directives, 'road-width', 'line-width', 1)
}

function roadGapWidth(directives) {
    return mergeInterpolatedDirective(directives, 'road-gap-width', 'line-gap-width', 1)
}

function labelColor(directives) {
    return mergeInterpolatedColorDirective(directives, 'label-color', 'text-color', 6, 8, 'rgb(0, 0, 0)')
}


function labelSize(directives) {
    return mergeInterpolatedNumberDirective(directives, 'label-size', 'text-size', 6, 8, 4, 14)
}

function mergeInterpolatedDirective(directives, property, alias, value) {
    let cases = directives.flatMap((rule) => {
        if (rule[property]) {
            return [rule['filter'], rule[property]]
        } else {
            return []
        }
    })
    if (cases.length == 0) {
        return {}
    }
    return {
        [alias]: [
            'interpolate',
            ['exponential', 1.2],
            ['zoom'],
            5,
            0.2,
            20,
            ['case', ...cases, value],
        ],
    }
}

function mergeInterpolatedColorDirective(directives, property, alias, startZoom, endZoom, fallback) {
    const cases = directives.filter((rule) => rule[property]).map((rule) => {
        const propertyValue = rule[property]
        if (propertyValue instanceof Array) {
            return [rule['filter'], propertyValue]
        } else {
            return [rule['filter'], [propertyValue, propertyValue]]
        }
    })
    if (cases.length == 0) {
        return {}
    }
    return {
        [alias]: [
            'interpolate',
            ['linear'],
            ['zoom'],
            startZoom,
            ['case', ...(cases.flatMap((c) => ([c[0], c[1][0]]))), fallback],
            endZoom,
            ['case', ...(cases.flatMap((c) => ([c[0], c[1][1]]))), fallback],
        ],
    }
}

function mergeInterpolatedNumberDirective(directives, property, alias, startZoom, endZoom, offset, fallback) {
    let cases = []
    directives.forEach((rule) => {
        if (rule[property]) {
            cases.push([rule['filter'], rule[property]])
        }
    })
    if (cases.length == 0) {
        return {}
    }
    return {
        [alias]: [
            'interpolate',
            ['linear'],
            ['zoom'],
            startZoom,
            ['case', ...(cases.flatMap((c) => ([c[0], c[1]]))), fallback],
            endZoom,
            ['case', ...(cases.flatMap((c) => ([c[0], c[1] + offset]))), fallback],
        ],
    }
}

function groupBy(xs, key) {
    return xs.reduce(function (rv, x) {
        ;(rv[x[key]] = rv[x[key]] || []).push(x)
        return rv
    }, {})
}