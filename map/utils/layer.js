export default function layer(layer) {
    return {
        id: layer['id'],
        type: layer['type'],
        filter: filter(layer),
        source: layer['source'],
        'source-layer': layer['source-layer'],
        minzoom: layer['minzoom'],
        maxzoom: layer['maxzoom'],
        layout: layer['directives']
            ? Object.assign(
                {
                    ...textFont(layer),
                    ...textField(layer),
                    ...textSize(layer),
                    ...textMaxWidth(layer),
                    ...iconImage(layer),
                    ...lineSortKey(layer),
                    ...fillSortKey(layer),
                },
                layer['layout'],
            )
            : layer['layout'],
        paint: layer['directives']
            ? Object.assign(
                {
                    ...textColor(layer),
                    ...textHaloColor(layer),
                    ...textHaloWidth(layer),
                    ...iconColor(layer),
                    ...fillColor(layer),
                    ...fillOutlineColor(layer),
                    ...lineColor(layer),
                    ...lineWidth(layer),
                    ...lineGapWidth(layer),
                    ...roadWidth(layer),
                    ...roadGapWidth(layer),
                },
                layer['paint'],
            )
            : layer['paint'],
    }
}

function filter(layer) {
    if (layer['filter'] && layer['directives']) {
        return [
            'all',
            layer['filter'],
            ['any', ...layer['directives'].map((rule) => rule['filter'])],
        ]
    } else if (layer['directives']) {
        return ['any', ...layer['directives'].map((rule) => rule['filter'])]
    } else if (layer['filter']) {
        return layer['filter']
    } else {
        return []
    }
}


function iconImage(layer) {
    return mergeDirectives(layer, 'icon-image', 'none')
}

function iconColor(layer) {
    return mergeDirectives(layer, 'icon-color', 'rgba(0, 0, 0, 0)')
}

function textFont(layer) {
    return mergeDirectives(layer, 'text-font', "Arial")
}

function textField(layer) {
    return mergeDirectives(layer, 'text-field', null)
}

function textSize(layer) {
    return mergeDirectives(layer, 'text-size', 12)
}

function textMaxWidth(layer) {
    return mergeDirectives(layer, 'text-max-width', 4)
}

function textColor(layer) {
    return mergeDirectives(layer, 'text-color', 'rgba(0, 0, 0, 0)')
}

function textHaloColor(layer) {
    return mergeDirectives(layer, 'text-halo-color', 'rgba(0, 0, 0, 0)')
}

function textHaloWidth(layer) {
    return mergeDirectives(layer, 'text-halo-width', 0)
}

function fillColor(layer) {
    return mergeDirectives(layer, 'fill-color', 'rgba(0, 0, 0, 0)')
}

function fillOutlineColor(layer) {
    return mergeDirectives(layer, 'fill-outline-color', 'rgba(0, 0, 0, 0)')
}

function lineColor(layer) {
    return mergeDirectives(layer, 'line-color', 'rgba(0, 0, 0, 0)')
}

function lineWidth(layer) {
    return mergeDirectives(layer, 'line-width', 0)
}

function lineGapWidth(layer) {
    return mergeDirectives(layer, 'line-gap-width', 0)
}

function lineSortKey(layer) {
    return mergeDirectives(layer, 'line-sort-key', 0)
}

function fillSortKey(layer) {
    return mergeDirectives(layer, 'fill-sort-key', 0)
}

function mergeDirectives(layer, property, value) {
    let cases = layer['directives'].flatMap((rule) => {
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

function roadWidth(layer) {
    return mergeInterpolatedDirective(layer, 'road-width', 'line-width', 1)
}

function roadGapWidth(layer) {
    return mergeInterpolatedDirective(layer, 'road-gap-width', 'line-gap-width', 1)
}

function mergeInterpolatedDirective(layer, property, alias, value) {
    let cases = layer['directives'].flatMap((rule) => {
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

function groupBy(xs, key) {
    return xs.reduce(function (rv, x) {
        ;(rv[x[key]] = rv[x[key]] || []).push(x)
        return rv
    }, {})
}
