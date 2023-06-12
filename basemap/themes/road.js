import style from './default.js';
import * as importedTools from "./tools.js";

function roadScheme(string) {
    const table = importedTools.cutRgbString(string);
    let redElement = Math.min(Math.max(table[0] + 50, importedTools.min), importedTools.max);
    return importedTools.giveNewColorString(table, redElement, table[1], table[2]);
}

export default Object.entries(style).reduce((acc, [key, value]) => {
    acc[key] = roadScheme(value);
    return acc;
}, {});


