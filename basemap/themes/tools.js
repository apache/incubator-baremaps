export const min = 0;
export const max = 255;

export function giveNewColorString(table, newRed, newGreen, newBlue) {
    let opacity = table[3];
    return `rgba(${newRed},${newGreen},${newBlue},${opacity})`;
};

export function cutRgbString(color) {
    let rgb = color.replace(/\s*/g, '').match(/rgb\((\d*)\,(\d*)\,(\d*)\)/)
    if (rgb != null) {
        return [
            parseInt(rgb[1]),
            parseInt(rgb[2]),
            parseInt(rgb[3]),
            1,
        ]
    }
    ;
    let rgba = color.replace(/\s*/g, '').match(/rgba\((\d*)\,(\d*)\,(\d*)\,(.*)\)/)
    if (rgba != null) {

        return [
            parseInt(rgba[1]),
            parseInt(rgba[2]),
            parseInt(rgba[3]),
            parseFloat(rgba[4]),
        ]
    }
    ;
    return null;
}

export function clamp(value) {
    return Math.min(Math.max(value, min), max);
}
