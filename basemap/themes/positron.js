import style from './default.js';
import * as importedTools from "./tools.js";

export function positronScheme(string) {
    const table = importedTools.cutRgbString(string)
    let newColor = positronColorCalculator(table);

    return importedTools.giveNewColorString(table, newColor, newColor, newColor);
};

function positronColorCalculator(table) {
    return Math.round((parseInt(table[0]) + parseInt(table[1]) + parseInt(table[2])) / 3);
};

export default Object.entries(style).reduce((acc, [key, value]) => {
    acc[key] = positronScheme(value);
    return acc;
}, {});
