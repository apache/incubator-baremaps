/**
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to you under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 **/

/**
 * Maplibre control to toggle the split view with another map.
 * 
 * Both maps must be in a container of display: flex. The control will toggle the flex property of
 * the map container between 0 and 1.
 * @see https://maplibre.org/maplibre-gl-js-docs/api/markers/#icontrol
 */
class MaplibreMapSplitViewToggle {
    constructor({ splitMap, splitMapContainerId }) {
        this._splitMap = splitMap;
        this._splitMapContainerId = splitMapContainerId;
    }
    /**
     * Add the control to the map.
     * @param {maplibre.Map} map the map
     * @returns {HTMLDivElement} the control
     */
    onAdd(map) {
        this._map = map;
        this._container = document.createElement('div');
        this._container.className = 'maplibregl-ctrl maplibregl-ctrl-group';
        // Add button to the container
        this._button = document.createElement('button');
        this._button.type = 'button';
        this._button.className = 'maplibregl-ctrl-icon maplibregl-ctrl-split-view';
        // Toggle the split view
        this._button.onclick = () => {
            const splitMapContainer = document.getElementById(this._splitMapContainerId);
            const state = splitMapContainer.getAttribute('data-state');
            if (state === 'visible') {
                // Hide the osm map
                splitMapContainer.setAttribute('data-state', 'hidden');
                splitMapContainer.style.flex = '0';
                this._map.resize();
                this._splitMap.resize();
                this._button.style.backgroundColor = '';
            } else {
                // Show the osm map
                splitMapContainer.setAttribute('data-state', 'visible');
                splitMapContainer.style.flex = '1';
                this._map.resize();
                this._splitMap.resize();
                this._button.style.backgroundColor = 'rgb(0 0 0/20%)';
            }
        };
        this._container.appendChild(this._button);
        return this._container;
    }

    /**
     * Remove the control from the map.
     */
    onRemove() {
        this._container.parentNode.removeChild(this._container);
        this._button = undefined;
        this._map = undefined;
    }
}
 