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
    os.mkdir(outdir)
    img = Image.open(image_path)
    file = open(json_path)
    pokedex_json = json.load(file)

    for item in pokedex_json:
        position = pokedex_json[item]["num"]
        pokemon_name = pokedex_json[item]["species"].lower().replace("-","_").replace(" ","").replace(".","").replace("'","")
        if position > 0:
            cut_sprite(img, outdir + "/smallicons_" + pokemon_name + ".png", 40, 30, position)
    
    #unknown ?
    cut_sprite(img, outdir + "/smallicons_0.png", 40, 30, 0)

    #forms
    cut_sprite(img, outdir + "/smallicons_"+"unfezantf.png", 40, 30, 788)
    cut_sprite(img, outdir + "/smallicons_"+"frillishf.png", 40, 30, 801)
    cut_sprite(img, outdir + "/smallicons_"+"jellicentf.png", 40, 30, 802)
    cut_sprite(img, outdir + "/smallicons_"+"meowsticf.png", 40, 30, 809)
    #CAP
    cut_sprite(img, outdir + "/smallicons_"+"syclant.png", 40, 30, 832)
    cut_sprite(img, outdir + "/smallicons_"+"revenankh.png", 40, 30, 832 + 1)
    cut_sprite(img, outdir + "/smallicons_"+"pyroak.png", 40, 30, 832 + 2)
    cut_sprite(img, outdir + "/smallicons_"+"fidgit.png", 40, 30, 832 + 3)
    cut_sprite(img, outdir + "/smallicons_"+"stratagem.png", 40, 30, 832 + 4)
    cut_sprite(img, outdir + "/smallicons_"+"arghonaut.png", 40, 30, 832 + 5)
    cut_sprite(img, outdir + "/smallicons_"+"kitsunoh.png", 40, 30, 832 + 6)
    cut_sprite(img, outdir + "/smallicons_"+"cyclohm.png", 40, 30, 832 + 7)
    cut_sprite(img, outdir + "/smallicons_"+"colossoil.png", 40, 30, 832 + 8)
    cut_sprite(img, outdir + "/smallicons_"+"krilowatt.png", 40, 30, 832 + 9)
    cut_sprite(img, outdir + "/smallicons_"+"voodoom.png", 40, 30, 832 + 10)
    cut_sprite(img, outdir + "/smallicons_"+"tomohawk.png", 40, 30, 832 + 11)
    cut_sprite(img, outdir + "/smallicons_"+"necturna.png", 40, 30, 832 + 12)
    cut_sprite(img, outdir + "/smallicons_"+"mollux.png", 40, 30, 832 + 13)
    cut_sprite(img, outdir + "/smallicons_"+"aurumoth.png", 40, 30, 832 + 14)
    cut_sprite(img, outdir + "/smallicons_"+"malaconda.png", 40, 30, 832 + 15)
    cut_sprite(img, outdir + "/smallicons_"+"cawmodore.png", 40, 30, 832 + 16)
    cut_sprite(img, outdir + "/smallicons_"+"volkraken.png", 40, 30, 832 + 17)
    #forms 2
    
    cut_sprite(img, outdir + "/smallicons_"+"aegislashblade.png", 40, 30, 681)
    cut_sprite(img, outdir + "/smallicons_"+"nidoranm.png", 40, 30, 32)
    cut_sprite(img, outdir + "/smallicons_"+"nidoranf.png", 40, 30, 29)
    cut_sprite(img, outdir + "/smallicons_"+"pichuspikyeared.png", 40, 30, 172)
    cut_sprite(img, outdir + "/smallicons_"+"porygonz.png", 40, 30, 474)
    cut_sprite(img, outdir + "/smallicons_"+"hooh.png", 40, 30, 250)
    cut_sprite(img, outdir + "/smallicons_"+"genesectdouse.png", 40, 30, 649)
    cut_sprite(img, outdir + "/smallicons_"+"genesectshock.png", 40, 30, 649)
    cut_sprite(img, outdir + "/smallicons_"+"genesectburn.png", 40, 30, 649)
    cut_sprite(img, outdir + "/smallicons_"+"genesectchill.png", 40, 30, 649)
    cut_sprite(img, outdir + "/smallicons_"+"arceusbug.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusdark.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusdragon.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceuselectric.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusfairy.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusfighting.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusfire.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusflying.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusghost.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusgrass.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusground.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusice.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceuspoison.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceuspsychic.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceusrock.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceussteel.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"arceuswater.png", 40, 30, 493)
    cut_sprite(img, outdir + "/smallicons_"+"pumpkaboosmall.png", 40, 30, 710)
    cut_sprite(img, outdir + "/smallicons_"+"pumpkaboolarge.png", 40, 30, 710)
    cut_sprite(img, outdir + "/smallicons_"+"pumpkaboosuper.png", 40, 30, 710)
    cut_sprite(img, outdir + "/smallicons_"+"gourgeistsmall.png", 40, 30, 711)
    cut_sprite(img, outdir + "/smallicons_"+"gourgeistlarge.png", 40, 30, 711)
    cut_sprite(img, outdir + "/smallicons_"+"gourgeistsuper.png", 40, 30, 711)
    cut_sprite(img, outdir + "/smallicons_"+"egg.png", 40, 30, 731)
    cut_sprite(img, outdir + "/smallicons_"+"rotomfan.png", 40, 30, 779)
    cut_sprite(img, outdir + "/smallicons_"+"rotomfrost.png", 40, 30, 780)
    cut_sprite(img, outdir + "/smallicons_"+"rotomheat.png", 40, 30, 781)
    cut_sprite(img, outdir + "/smallicons_"+"rotommow.png", 40, 30, 782)
    cut_sprite(img, outdir + "/smallicons_"+"rotomwash.png", 40, 30, 783)
    cut_sprite(img, outdir + "/smallicons_"+"giratinaorigin.png", 40, 30, 785)
    cut_sprite(img, outdir + "/smallicons_"+"shayminsky.png", 40, 30, 787)
    cut_sprite(img, outdir + "/smallicons_"+"basculinbluestriped.png", 40, 30, 789)
    cut_sprite(img, outdir + "/smallicons_"+"darmanitanzen.png", 40, 30, 792)
    cut_sprite(img, outdir + "/smallicons_"+"deoxysattack.png", 40, 30, 763)
    cut_sprite(img, outdir + "/smallicons_"+"deoxysdefense.png", 40, 30, 764)
    cut_sprite(img, outdir + "/smallicons_"+"deoxysspeed.png", 40, 30, 766)
    cut_sprite(img, outdir + "/smallicons_"+"wormadamsandy.png", 40, 30, 771)
    cut_sprite(img, outdir + "/smallicons_"+"wormadamtrash.png", 40, 30, 772)
    cut_sprite(img, outdir + "/smallicons_"+"cherrimsunshine.png", 40, 30, 774)
    cut_sprite(img, outdir + "/smallicons_"+"castformrainy.png", 40, 30, 760)
    cut_sprite(img, outdir + "/smallicons_"+"castformsnowy.png", 40, 30, 761)
    cut_sprite(img, outdir + "/smallicons_"+"castformsunny.png", 40, 30, 762)
    cut_sprite(img, outdir + "/smallicons_"+"meloettapirouette.png", 40, 30, 804)
    cut_sprite(img, outdir + "/smallicons_"+"meowsticf.png", 40, 30, 809)
    cut_sprite(img, outdir + "/smallicons_"+"floetteeternalflower.png", 40, 30, 810)
    cut_sprite(img, outdir + "/smallicons_"+"tornadustherian.png", 40, 30, 816)
    cut_sprite(img, outdir + "/smallicons_"+"thundurustherian.png", 40, 30, 817)
    cut_sprite(img, outdir + "/smallicons_"+"landorustherian.png", 40, 30, 818)
    cut_sprite(img, outdir + "/smallicons_"+"kyuremblack.png", 40, 30, 819)
    cut_sprite(img, outdir + "/smallicons_"+"kyuremwhite.png", 40, 30, 820)
    cut_sprite(img, outdir + "/smallicons_"+"keldeoresolute.png", 40, 30, 821)
    cut_sprite(img, outdir + "/smallicons_"+"hoopaunbound.png", 40, 30, 70*12+10)


                
cut_items('itemicons-sheet.png', 'item.json', 'out_items')
cut_pokedex('xyicons-sheet.png', 'pokedex.json', 'out_icons')