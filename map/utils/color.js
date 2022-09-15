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

function readColor(color) {
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
