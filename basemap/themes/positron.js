import style from './default.js';
import importedTools from "./tools.js;"

function positronScheme(string) {
    const table = importedTools.cutRgbString(string)
    let newColor = positronColorCalculator(table);    

    return importedTools.giveNewColorString(table, newColor, newColor, newColor);
};

function positronColorCalculator(table) {
    return Math.round((parseInt(table[0]) + parseInt(table[1]) + parseInt(table[2])) / 3);
};

function moreRedScheme(string) {
    const table = importedTools.cutRgbString(string);
    let redElement = Math.min(Math.max(table[0] + 50, min), max);
    return importedTools.giveNewColorString(table, redElement, table[1], table[2]);
}



export default Object.entries(style).reduce((acc, [key, value]) => {
    acc[key] = positronScheme(value);
    return acc;
}, {});