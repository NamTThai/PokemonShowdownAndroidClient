from PIL import Image
import os
import json

def cut_sprite(image, output, cut_width, cut_height, position):
    nb_cols = int(image.width/cut_width)
    nb_rows = int(image.height/cut_height)
    i = int(position%nb_cols)
    j = int(position/nb_cols)
    bbox = (i*cut_width, j*cut_height, ((i+1)*cut_width), ((j+1)*cut_height))
    working_slice = image.crop(bbox)
    working_slice.save(output)

def cut_items(image_path, json_path, outdir):
    if os.path.isdir(outdir) == False:
        os.mkdir(outdir)
    img = Image.open(image_path)
    file = open(json_path)
    item_json = json.load(file)

    for item in item_json:
        position = item_json[item]["spritenum"]
        item_name = item_json[item]["id"]
        if position > 0:
            cut_sprite(img, outdir + "/item_" + str(item_name) + ".png", 24, 24, position)

def cut_pokedex(image_path, json_path, outdir):
    if os.path.isdir(outdir) == False:
        os.mkdir(outdir)
    img = Image.open(image_path)
    file = open(json_path)
    pokedex_json = json.load(file)

    for item in pokedex_json:
        position = pokedex_json[item]["num"]
        pokemon_name = item.lower().replace("-","_").replace(" ","").replace(".","").replace("'","")
        if position > 0:
            cut_sprite(img, outdir + "/smallicons_" + pokemon_name + ".png", 40, 30, position)
    
    #unknown ?
    cut_sprite(img, outdir + "/smallicons_0.png", 40, 30, 0)

    #taken from battledata.js
    alternameforms = {
			"egg": 804 + 1,
            "pikachubelle": 804 + 2,
            "pikachulibre": 804 + 3,
            "pikachuphd": 804 + 4,
            "pikachupopstar": 804 + 5,
            "pikachurockstar": 804 + 6,
            "pikachucosplay": 804 + 7,
            "castformrainy": 804 + 35,
            "castformsnowy": 804 + 36,
            "castformsunny": 804 + 37,
            "deoxysattack": 804 + 38,
            "deoxysdefense": 804 + 39,
            "deoxysspeed": 804 + 40,
            "burmysandy": 804 + 41,
            "burmytrash": 804 + 42,
            "wormadamsandy": 804 + 43,
            "wormadamtrash": 804 + 44,
            "cherrimsunshine": 804 + 45,
            "shelloseast": 804 + 46,
            "gastrodoneast": 804 + 47,
            "rotomfan": 804 + 48,
            "rotomfrost": 804 + 49,
            "rotomheat": 804 + 50,
            "rotommow": 804 + 51,
            "rotomwash": 804 + 52,
            "giratinaorigin": 804 + 53,
            "shayminsky": 804 + 54,
            "unfezantf": 804 + 55,
            "basculinbluestriped": 804 + 56,
            "darmanitanzen": 804 + 57,
            "deerlingautumn": 804 + 58,
            "deerlingsummer": 804 + 59,
            "deerlingwinter": 804 + 60,
            "sawsbuckautumn": 804 + 61,
            "sawsbucksummer": 804 + 62,
            "sawsbuckwinter": 804 + 63,
            "frillishf": 804 + 64,
            "jellicentf": 804 + 65,
            "tornadustherian": 804 + 66,
            "thundurustherian": 804 + 67,
            "landorustherian": 804 + 68,
            "kyuremblack": 804 + 69,
            "kyuremwhite": 804 + 70,
            "keldeoresolute": 804 + 71,
            "meloettapirouette": 804 + 72,
            "vivillonarchipelago": 804 + 73,
            "vivilloncontinental": 804 + 74,
            "vivillonelegant": 804 + 75,
            "vivillonfancy": 804 + 76,
            "vivillongarden": 804 + 77,
            "vivillonhighplains": 804 + 78,
            "vivillonicysnow": 804 + 79,
            "vivillonjungle": 804 + 80,
            "vivillonmarine": 804 + 81,
            "vivillonmodern": 804 + 82,
            "vivillonmonsoon": 804 + 83,
            "vivillonocean": 804 + 84,
            "vivillonpokeball": 804 + 85,
            "vivillonpolar": 804 + 86,
            "vivillonriver": 804 + 87,
            "vivillonsandstorm": 804 + 88,
            "vivillonsavanna": 804 + 89,
            "vivillonsun": 804 + 90,
            "vivillontundra": 804 + 91,
            "pyroarf": 804 + 92,
            "flabebeblue": 804 + 93,
            "flabebeorange": 804 + 94,
            "flabebewhite": 804 + 95,
            "flabebeyellow": 804 + 96,
            "floetteblue": 804 + 97,
            "floetteeternal": 804 + 98,
            "floetteorange": 804 + 99,
            "floettewhite": 804 + 100,
            "floetteyellow": 804 + 101,
            "florgesblue": 804 + 102,
            "florgesorange": 804 + 103,
            "florgeswhite": 804 + 104,
            "florgesyellow": 804 + 105,
            "meowsticf": 804 + 115,
            "aegislashblade": 804 + 116,
            "hoopaunbound": 804 + 118,
            "rattataalola": 804 + 119,
            "raticatealola": 804 + 120,
            "raichualola": 804 + 121,
            "sandshrewalola": 804 + 122,
            "sandslashalola": 804 + 123,
            "vulpixalola": 804 + 124,
            "ninetalesalola": 804 + 125,
            "diglettalola": 804 + 126,
            "dugtrioalola": 804 + 127,
            "meowthalola": 804 + 128,
            "persianalola": 804 + 129,
            "geodudealola": 804 + 130,
            "graveleralola": 804 + 131,
            "golemalola": 804 + 132,
            "grimeralola": 804 + 133,
            "mukalola": 804 + 134,
            "exeggutoralola": 804 + 135,
            "marowakalola": 804 + 136,
            "greninjaash": 804 + 137,
            "zygarde10": 804 + 138,
            "zygardecomplete": 804 + 139,
            "oricoriopompom": 804 + 140,
            "oricoriopau": 804 + 141,
            "oricoriosensu": 804 + 142,
            "lycanrocmidnight": 804 + 143,
            "wishiwashischool": 804 + 144,
            "miniorred": 804 + 145,
            "miniororange": 804 + 146,
            "minioryellow": 804 + 147,
            "miniorgreen": 804 + 148,
            "miniorblue": 804 + 149,
            "miniorviolet": 804 + 150,
            "miniorindigo": 804 + 151,
            "magearnaoriginal": 804 + 152,
            "pikachuoriginal": 804 + 153,
            "pikachuhoenn": 804 + 154,
            "pikachusinnoh": 804 + 155,
            "pikachuunova": 804 + 156,
            "pikachukalos": 804 + 157,
            "pikachualola": 804 + 158,
            "venusaurmega": 972 + 0,
            "charizardmegax": 972 + 1,
            "charizardmegay": 972 + 2,
            "blastoisemega": 972 + 3,
            "beedrillmega": 972 + 4,
            "pidgeotmega": 972 + 5,
            "alakazammega": 972 + 6,
            "slowbromega": 972 + 7,
            "gengarmega": 972 + 8,
            "kangaskhanmega": 972 + 9,
            "pinsirmega": 972 + 10,
            "gyaradosmega": 972 + 11,
            "aerodactylmega": 972 + 12,
            "mewtwomegax": 972 + 13,
            "mewtwomegay": 972 + 14,
            "ampharosmega": 972 + 15,
            "steelixmega": 972 + 16,
            "scizormega": 972 + 17,
            "heracrossmega": 972 + 18,
            "houndoommega": 972 + 19,
            "tyranitarmega": 972 + 20,
            "sceptilemega": 972 + 21,
            "blazikenmega": 972 + 22,
            "swampertmega": 972 + 23,
            "gardevoirmega": 972 + 24,
            "sableyemega": 972 + 25,
            "mawilemega": 972 + 26,
            "aggronmega": 972 + 27,
            "medichammega": 972 + 28,
            "manectricmega": 972 + 29,
            "sharpedomega": 972 + 30,
            "cameruptmega": 972 + 31,
            "altariamega": 972 + 32,
            "banettemega": 972 + 33,
            "absolmega": 972 + 34,
            "glaliemega": 972 + 35,
            "salamencemega": 972 + 36,
            "metagrossmega": 972 + 37,
            "latiasmega": 972 + 38,
            "latiosmega": 972 + 39,
            "kyogreprimal": 972 + 40,
            "groudonprimal": 972 + 41,
            "rayquazamega": 972 + 42,
            "lopunnymega": 972 + 43,
            "garchompmega": 972 + 44,
            "lucariomega": 972 + 45,
            "abomasnowmega": 972 + 46,
            "gallademega": 972 + 47,
            "audinomega": 972 + 48,
            "dianciemega": 972 + 49,

            "syclant": 1140 + 0,
            "revenankh": 1140 + 1,
            "pyroak": 1140 + 2,
            "fidgit": 1140 + 3,
            "stratagem": 1140 + 4,
            "arghonaut": 1140 + 5,
            "kitsunoh": 1140 + 6,
            "cyclohm": 1140 + 7,
            "colossoil": 1140 + 8,
            "krilowatt": 1140 + 9,
            "voodoom": 1140 + 10,
            "tomohawk": 1140 + 11,
            "necturna": 1140 + 12,
            "mollux": 1140 + 13,
            "aurumoth": 1140 + 14,
            "malaconda": 1140 + 15,
            "cawmodore": 1140 + 16,
            "volkraken": 1140 + 17,
            "plasmanta": 1140 + 18,
            "naviathan": 1140 + 19,
            "crucibelle": 1140 + 20,
            "crucibellemega": 1140 + 21,
            "kerfluffle": 1140 + 22
		}
    
    for name in alternameforms:
        cut_sprite(img, outdir + "/smallicons_" + name + ".png", 40, 30, alternameforms[name])
   
#cut_items('itemicons-sheet.png', 'item.json', 'out_items')
cut_pokedex('smicons-sheet.png', 'pokedex.json', 'out_icons')