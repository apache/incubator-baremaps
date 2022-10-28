function container(child, show) {
    var container = document.createElement('div');
    container.className = 'maplibregl-ctrl maplibregl-ctrl-group';
    container.appendChild(child);
    if (!show) {
        container.style.display = 'none';
    }
    return container;
}

function button() {
    var btn = document.createElement('button');
    btn.className = 'maplibregl-ctrl-icon maplibregl-ctrl-boundaries';
    btn.type = 'button';
    btn['aria-label'] = 'Inspect';
    return btn;
}

function BoundariesButton(options) {
    options = Object.assign({
        show: true,
        onToggle: function () {},
    }, options);
    this._btn = button();
    this._btn.onclick = options.onToggle;
    this.elem = container(this._btn, options.show);
}

BoundariesButton.prototype.setShowTileBoundariesIcon = function () {
    this._btn.className = 'maplibregl-ctrl-icon maplibregl-ctrl-show-boundaries';
};

BoundariesButton.prototype.setHideTileBoundariesIcon = function () {
    this._btn.className = 'maplibregl-ctrl-icon maplibregl-ctrl-hide-boundaries';
};

function MaplibreTileBoundaries(options) {
    if (!(this instanceof MaplibreTileBoundaries)) {
        throw new Error('MaplibreTileBoundaries needs to be called with the new keyword');
    }

    this.options = Object.assign({
        showTileBoundariesButton: true,
        tileBoundaries: false,
    }, options);

    this._tileBoundaries = this.options.tileBoundaries;
    this._toggle = new BoundariesButton({
        show: this.options.showTileBoundariesButton,
        onToggle: this.toggleTileBoundaries.bind(this)
    });
}

MaplibreTileBoundaries.prototype.toggleTileBoundaries = function () {
    this._tileBoundaries = !this._tileBoundaries;
    this.render();
};

MaplibreTileBoundaries.prototype.render = function () {
    if (this._tileBoundaries) {
        this._map.showTileBoundaries = true;
        this._toggle.setHideTileBoundariesIcon();
    } else {
        this._map.showTileBoundaries = false;
        this._toggle.setShowTileBoundariesIcon();
    }
};

MaplibreTileBoundaries.prototype.onAdd = function (map) {
    this._map = map;
    this.render();
    return this._toggle.elem;
};

MaplibreTileBoundaries.prototype.onRemove = function () {
    this._map = undefined;
};
