import * as importedTools from "./tools.js";
import positronScheme from "./positron.js";

const CLARITYFACTOR = 4.7;


function lightScheme(string) {
    const table = importedTools.cutRgbString(string);
    if (table[1] != null) {
        let newColor = importedTools.max - importedTools.clamp(table[0] / CLARITYFACTOR);
        return importedTools.giveNewColorString(table, newColor, newColor, newColor);
    }
    return null;
}

export default Object.entries(positronScheme).reduce((acc, [key, value]) => {
    acc[key] = lightScheme(value);
    return {
        ...acc,
        attractionStyleWaterSlideLineColor: 'rgb(175,175,175)',
        landuseBackgroundBasinFillColor: 'rgb(175,175,175)',
        leisureBackgroundSwimmingPoolFillColor: 'rgb(175,175,175)',
        leisureOverlaySwimmingPoolFillOutlineColor: 'rgb(175,175,175)',
        naturalBackgroundWaterFillColor: 'rgb(175,175,175)',
        naturalOverlayLakeFillColor: 'rgb(175,175,175)',
        naturalWaterFillColor: 'rgb(175,175,175)',
        oceanWaterFillColor: 'rgb(175,175,175)',
        pointCountryLabelCountryTextColor: 'rgb(90, 56, 90)',
        pointCountryLabelPaintTextHaloColor: 'rgba(255, 255, 255, 0.8)',
        pointIconWaterfallIconColor: 'rgb(175,175,175)',
        pointIconWaterfallTextColor: 'rgb(175,175,175)',
        pointLabelCityFilterOneLabelColor: 'rgb(100, 100, 100)',
        pointLabelCityFilterTwoLabelColor: 'rgb(50, 50, 50)',
        pointLabelCityLabelColor: 'rgb(25, 25, 25)',
        pointLabelLocalityLabelColor: 'rgb(100, 100, 100)',
        pointLabelPaintTextHaloColor: 'rgba(255, 255, 255, 0.8)',
        pointLabelPlaceTextColor: 'rgba(100, 100, 100, 1)',
        pointLabelTownFilterOneLabelColor: 'rgb(100, 100, 100)',
        pointLabelTownFilterTwoLabelColor: 'rgb(75, 75, 75)',
        pointLabelVillageLabelColor: 'rgb(100, 100, 100)',
        waterwayLabelTextColor: 'rgb(175,175,175)',
        waterwayLabelTextHaloColor: 'rgb(175,175,175)',
        waterwayLineWaterwayLineColor: 'rgb(175,175,175)',
        waterwayTunnelCasingLineColor: 'rgb(175,175,175)',
        waterwayTunnelLineLineColor: 'rgb(175,175,175)',
    };


}, {});
