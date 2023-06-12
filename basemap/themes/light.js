import style from './default.js';
import * as importedTools from "./tools.js";
import positronScheme from "./positron.js";


function lightScheme(string){
    const table=importedTools.cutRgbString(string);
    if(table[1]!=null){
    let newColor=importedTools.max-Math.min(Math.max(table[1]/4.7, importedTools.min),importedTools.max);
    return importedTools.giveNewColorString(table, newColor, newColor, newColor);
    }
    return null;
}

export default Object.entries(positronScheme).reduce((acc, [key, value]) => {
    acc[key] = lightScheme(value);
    return acc;

    
}, {});