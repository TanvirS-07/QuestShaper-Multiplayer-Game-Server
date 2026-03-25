
// Map viewing values.
const tile_width = 64;
const tile_height = 64;

var view_width = 11;
var view_height = 11;
var view_middleX = view_width >> 1;
var view_middleY = view_height >> 1;

// Where to draw the player on screen.
var playerX = 5;
var playerY = 5;

var default_width = tile_width * view_width;
var default_height = tile_height * view_height;

// Offset for drawing water and other "cycling" tiles.
var offsetX = 0;
var offsetY = 0;

// Wind direction, used for "cycling" tiles.
var windX = +1;
var windY = +1;

// Position of the viewer relative to the map.
var posX = 5;
var posY = 5;

// Timer settings.
const animation_time = 100; // 100 milliseconds = 10 frames per second.

// Server address.
var server_addr = "http://localhost:8000";

// Location of asset files.
const asset_path = "assets/64x64/";

// Map data.
var map_width = 20;
var map_height = 20;

var map = [
	['g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', '.', 'W', 'g', 't', 't', 'g', 'g', 't', 'g', 'g', 'g', ],
	['S', 'S', 'S', 'S', 'S', 'S', 'g', 'g', 'g', 'g', 'W', 'g', 't', ';', 't', 't', 't', 'g', 'g', 'g', ],
	['S', 'w', 'w', 'w', 'w', 'S', 'g', 'g', 'g', 'W', 'W', 'g', 't', 't', 't', 't', 't', 't', 'g', 'g', ],
	['S', 'w', 'w', 'w', 'w', 'S', 'g', 'g', 'W', 'W', 'g', 'g', 't', 't', 't', 't', 't', 'g', 'g', 'g', ],
	['S', 'S', 'S', 'D', 'S', 'S', 'g', 'g', 'W', 'g', 'g', 't', 't', 't', 'g', 'g', 'g', 'g', 'g', 'g', ],
	['g', 'g', ',', '_', 'g', 'g', 'g', 'W', 'W', 'W', 'g', 't', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', ],
	['.', 'g', 'g', '_', 'g', 'g', 'g', 'W', 'g', 'W', '.', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', ],
	['_', '_', '_', '_', 'g', 'g', 'W', 'W', 'g', 'W', 'W', 'g', 'g', 'g', 'g', '_', '_', '_', '_', '_', ],
	['_', 'g', 't', 't', 'g', 'W', 'W', 'W', 'g', 'g', 'W', 'g', 'g', 'g', 'g', '_', 'g', 'g', 'g', 'g', ],
	['_', 'g', 'g', 't', 'g', 'W', 'W', 'g', 'g', 'g', 'W', 'g', 'g', 'g', 'B', 'D', 'B', 'g', 'g', 'g', ],
	['_', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'W', 'W', 'g', 'g', 'B', 'f', 'B', '.', 'g', 'g', ],
	['_', 'g', 'g', 'g', 'g', 'g', 't', 't', 'g', 'W', 'W', 'g', 'g', 'B', 'B', 'f', 'B', 'B', 'g', 'g', ],
	['_', 'g', 'g', 'g', 'g', 'g', 't', 'g', 'g', 'W', ',', 'g', 'g', 'B', 'f', 'f', 'f', 'B', 'g', 'g', ],
	['_', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'W', 'W', 'g', 'g', 'B', 'f', 'f', 'f', 'B', 'g', 'g', ],
	['_', '_', 'g', 'g', 'g', 'g', 'g', 'g', 'W', '.', 'W', 'g', 'g', 'B', 'f', 'f', 'f', 'B', 'g', 'g', ],
	['g', '_', '_', 'g', 'g', 'g', 'g', 'W', 'W', 'g', 'W', 'g', 'g', 'B', 'B', 'B', 'B', 'B', 'g', 'g', ],
	['g', 'g', '_', 'g', 'g', 'g', 'g', 'W', 'g', 'g', 'W', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', ],
	['g', 'g', '_', 'g', 'g', 'g', 'W', 'W', 'g', 'g', 'W', 'W', 'g', 'g', ':', 'g', 'g', 'g', 'g', 'g', ],
	['g', 'g', 'g', 'g', 'g', 'W', 'W', 'W', 'g', 'g', 'W', 'W', 'g', 'g', 'g', 'g', 't', 'g', 'g', 'g', ],
	['g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'W', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', 'g', ],
];

function draw_image(dst, dst_x, dst_y, src, src_x, src_y, src_width, src_height) {
	dst.drawImage(src, src_x, src_y, src_width, src_height, dst_x, dst_y, src_width, src_height);
}

function draw_image_offset(dst, dst_x, dst_y, src, src_x, src_y, src_width, src_height, offset_x, offset_y) {
	if ((offset_x == 0) && (offset_y == 0)) {
		draw_image(dst, dst_x, dst_y, src, src_x, src_y, src_width, src_height);
		return;
	}
	if (offset_x < 0) {
		offset_x += src_width;
	} else if (offset_x >= src_width) {
		offset_x -= src_width;
	}
	if (offset_y < 0) {
		offset_y += src_height;
	} else if (offset_y >= src_height) {
		offset_y -= src_height;
	}
	draw_image(dst, dst_x, dst_y,
		src, src_x + src_width - offset_x, src_y + src_height - offset_y, offset_x, offset_y);
	draw_image(dst, dst_x + offset_x, dst_y,
		src, src_x, src_y + src_height - offset_y, src_width - offset_x, offset_y);
	draw_image(dst, dst_x, dst_y + offset_y,
		src, src_x + src_width - offset_x, src_y, offsetX, src_height - offset_y);
	draw_image(dst, dst_x + offset_x, dst_y + offset_y,
		src, src_x, src_y, src_width - offset_x, src_height - offset_y);
}

class Terrain {
	constructor(kind, tile, blocking, cycles, filename) {
		this.kind = kind;
		this.tile = tile;
		this.blocking = blocking;
		this.cycles = cycles;
		this.filename = filename;
	}
}

var terrains = [
	new Terrain( 'B', null, true,  false, "brickwall.png"    ),
	new Terrain( 'b', null, false, false, "bridge.png"       ),
	new Terrain( 'D', null, true,  false, "doorsclosed.png"  ),
	new Terrain( 'd', null, false, false, "doorsopened.png"  ),
	new Terrain( '_', null, false, false, "dirt.png"         ),
	new Terrain( 'f', null, false, false, "flagstones.png"   ),
	new Terrain( 'g', null, false, false, "grass.png"        ),
	new Terrain( 'p', null, false, false, "pebbles.png"      ),
	new Terrain( '.', null, false, false, "rocks1.png"       ),
	new Terrain( ',', null, false, false, "rocks2.png"       ),
	new Terrain( ':', null, false, false, "rocks3.png"       ),
	new Terrain( ';', null, false, false, "rocks6.png"       ),
	new Terrain( 's', null, false, false, "sand.png"         ),
	new Terrain( 'S', null, true,  false, "stonewall.png"    ),
	new Terrain( 't', null, false, false, "tree.png"         ),
	new Terrain( 'W', null, true,  true,  "waterwaves.png"   ),
	new Terrain( 'w', null, false, false, "woodenboards.png" ),
];

var player1_filename = "player1.png";
var player_tile = null;

function find_terrain_by_kind(kind) {
	for (let  t = 0; t < terrains.length; t++) {
		if (terrains[t].kind == kind) {
			return terrains[t];
		}
	}
	return null;
}

function is_blocking(terrain) {
	if (terrain == null) {
		return true;
	}
	return terrain.blocking;
}

async function load_all_terrain_tiles() {
	for (let t = 0; t < terrains.length; t++) {
		tile = new Image();
		tile.src = asset_path + terrains[t].filename;
		await tile.decode();
		terrains[t].tile = tile;
	}
}

async function load_all_player_tiles() {
	player_tile = new Image();
	player_tile.src = asset_path + player1_filename;
	await player_tile.decode();
}

function fix_x(x) {
	if (x < 0) {
		x += map_width;
	}
	if (x >= map_width) {
		x -= map_width;
	}
	return x;
}

function fix_y(y) {
	if (y < 0) {
		y = 0;
	}
	if (y >= map_height) {
		y = map_height - 1;
	}
	return y;
}

function get_map_yx(abs_y, abs_x) {
	if ((abs_y < 0) || (abs_y >= map_height)) {
		return ' ';
	}
	if ((abs_x < 0) || (abs_x >= map_width)) {
		return ' ';
	}
	return map[abs_y][abs_x];
}

function set_map_yx(abs_y, abs_x, kind) {
	if ((abs_y < 0) || (abs_y >= map_height)) {
		return;
	}
	if ((abs_x < 0) || (abs_x >= map_width)) {
		return;
	}
	map[abs_y][abs_x] = kind;
}

function set_map_row(abs_y, abs_left, row_window) {
	if ((abs_y < 0) || (abs_y >= map_height)) {
		return; // Outside the map, so ignore it.
	}
	for (let src_x = 0; src_x < row_window.length; src_x++) {
		let x = fix_x(abs_left + src_x);
		if ((x < 0) || (x >= map_width)) {
			continue; // Outside the map, so ignore it.
		}
		map[abs_y][x] = row_window[src_x];
	}
}

function set_map_window(abs_top, abs_left, abs_bottom, abs_right, map_data) {
	for (let src_y = 0; src_y < map_data.length; src_y++) {
		let row_window = map_data[src_y];
		set_map_row(abs_top + src_y, abs_left, row_window);
	}
}

async function fetch_map(y, x) {
	try {
		const response = await fetch(server_addr + `/info?y=${y}&x=${x}`, {
			method: 'GET',
			headers: {'Content-type': 'application/json; charset=UTF-8'}
		});
		if (! response) {
			throw new Error(`HTTP error! info failed`);
		}
		if (! response.ok) {
			throw new Error(`HTTP error! info not ok status: ${response.status}`);
		}
		const result = await response.json();
		return result;
	} catch (error) {
		console.error('Error:', error);
		return null;
	}
}

async function fetch_map_and_refresh() {
	const result = await fetch_map(posY, posX);
	if (! result) {
		return;
	}
	posX = result.x;
	posY = result.y;
	set_map_window(result.top, result.left, result.bottom, result.right, result.info);
}

async function move_request(dy, dx) {
	try {
		const response = await fetch(server_addr + `/move?dy=${dy}&dx=${dx}`, {
			method: 'GET',
			headers: {'Content-type': 'text/plain; charset=UTF-8'}
		});
		if (! response) {
			throw new Error(`HTTP error! move failed`);
		}
		if (response.status != 200) {
			console.log(`HTTP error! move not 200 status: ${response.status}`);
			return "blocked";
		}
		return await response.text();
	} catch (error) {
		console.error('Error:', error);
		return "error";
	}
}

var gameArea = {
	canvas : document.getElementById("canvas"),
	server : document.getElementById("server"),
	beginBtn : document.getElementById("begin"),
	display_map: function() {
		for (let y = 0; y < view_height; y++) {
			var srcY = y + posY - view_middleY;

			for (let x = 0; x < view_width; x++) {
				var srcX = x + posX - view_middleX;
				if (srcX < 0) {
					srcX += map_height;
				} else if (srcX >= map_height) {
					srcX -= map_height;
				}

				var dst_x  = x * tile_width;
				var dst_y  = y * tile_height;

				if ((srcY < 0) || (srcY >= map_height)) {
					this.context.fillStyle = "black"
					this.context.fillRect(dst_x, dst_y, tile_width, tile_height);
					continue;
				}

				var kind = get_map_yx(srcY, srcX);

				var terrain = find_terrain_by_kind(kind);
				if ((terrain == null) || (terrain.tile == null)) {
					this.context.fillStyle = "black";
					this.context.fillRect(dst_x, dst_y, tile_width, tile_height);
					continue;
				}
				var tile = terrain.tile;
				if (terrain.cycles) {
					draw_image_offset(this.context, dst_x, dst_y,
						tile, 0, 0, tile_width, tile_height,
						offsetX, offsetY);
				} else {
					draw_image(this.context, dst_x, dst_y,
						tile, 0, 0, tile_width, tile_height);
				}
			}
		}
	},
	display_players: function() {
		var tile = player_tile;
		draw_image(this.context, playerX * tile_width, playerY * tile_height,
			tile, 0, 0, tile_width, tile_height);
	},
	draw_all: function() {
		this.display_map();
		this.display_players();
	},
	update: function() {
		// Cycle the offset.
		offsetX += windX;
		offsetY += windY;
		if (offsetX < 0) {
			offsetX += tile_width;
		} else if (offsetX >= tile_width) {
			offsetX = 0;
		}
		if (offsetY < 0) {
			offsetY += tile_height;
		} else if (offsetY >= tile_height) {
			offsetY = 0;
		}

		this.draw_all();
	},
	init_timer: function () {
		var me = this;
		var time_loop = function () {
			me.update();
			me.timer = setTimeout(time_loop, animation_time);
		};
		this.timer = setTimeout(time_loop, animation_time);
	},
	bind_key_events: function () {
		var me = this;
		var key_event = "keypress";
		if (this.isSafari() || this.isIE()) {
			key_event = "keydown";
		}
		var key_handler = function (e) {
			me.handle_key(e);
		};
		if (window.addEventListener) {
			document.addEventListener(key_event, key_handler, false);
		} else {
			document.attachEvent("on" + key_event, key_handler);
		}
	},
	bind_mouse_events: function () {
		var me = this;
		var mouse_event = "click";
		var mouse_handler = function (e) {
			me.handle_mouse(e);
		};
		var reset_handler = function (e) {
			me.handle_reset(e);
		};
		if (window.addEventListener) {
			this.canvas.addEventListener(mouse_event, mouse_handler, false);
			this.beginBtn.addEventListener(mouse_event, reset_handler, false);
		} else {
			this.canvas.attachEvent("on" + mouse_event, mouse_handler);
			this.beginBtn.attachEvent("on" + mouse_event, reset_handler);
		}
	},
	fix_pos: function() {
		posX = fix_x(posX);
		posY = fix_y(posY);
	},
	move_character: function(dy, dx) {
		const kind = get_map_yx(fix_y(posY + dy), fix_x(posX + dx));
		const terrain = find_terrain_by_kind(kind);
		if (! is_blocking(terrain)) {
			posY += dy;
			posX += dx;
			this.fix_pos();
		}

		// Tell server about the move.
		move_request(dy, dx);
	},
	handle_key: function (e) {
		var key = this.get_key(e);
		switch (key) {
			case 65: // A
			case 37: // left arrow
				this.move_character(-0, -1);
				break;
			case 87: // W
			case 38: // up arrow
				this.move_character(-1, -0);
				break;
			case 68: // D
			case 39: // right arrow
				this.move_character(+0, +1);
				break;
			case 83: // S
			case 40: // down arrow
				this.move_character(+1, +0);
				break;
			case 27: // escape key
				break;
			case 32: // space key
				break;
			default:
				break;
		}
		fetch_map_and_refresh();		
	},
	get_key: function (e) {
		if (window.event) {
			return window.event.keyCode;
		} else if (e) {
			return e.keyCode;
		}
		return 0;
	},
	handle_mouse: function (e) {
		console.log('Mouse click');
	},
	handle_reset: function (e) {
		this.reset();
	},
	isIE: function () {
		return this.browserTest(/IE/);
	},
	isFirefox: function () {
		return this.browserTest(/Firefox/);
	},
	isSafari: function () {
		return this.browserTest(/Safari/);
	},
	browserTest: function (rgx) {
		return rgx.test(navigator.userAgent);
	},
	reset: function() {
		server_addr = this.server.value;
		fetch_map_and_refresh();
	},
	start : function() {
		this.canvas.width = default_width;
		this.canvas.height = default_height;
		this.context = this.canvas.getContext("2d");
		this.bind_key_events();
		this.bind_mouse_events();
		this.init_timer();
		this.reset();
	}
};

async function startGame() {
	await load_all_terrain_tiles();
	await load_all_player_tiles();
	gameArea.start();
}

