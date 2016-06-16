import json
import random
import datetime
from os import listdir

jsonnam = 'rsegepersonal.json'

infold = 'c:/users/jon sege/dropbox/kumpi mayu/mapdev/samplePhotos/'
outfold = 'c:/users/jon sege/dropbox/kumpi mayu/mapdev/geojson/'

d = [e for e in listdir(infold) if '.jpg' in e]
for i, v in enumerate(d):
    print(i, v)

fn = input('Please select a photo, type \'note\' to enter a new note, or \'meas\' to enter a new measurement: ')
try:
    fn = int(fn)
except ValueError:
    fn = fn

if fn not in {'note', 'meas'}:
    if type(fn) is int:
        while True:
            try:
                fnam = d[fn]
                break
            except IndexError:
                fn = input('Please select a file number from the list: ')
    elif type(fn) is str:
        fnam = fn

    from photo.read_photo import *
    fp = infold + fnam
    props = read_photo_props(infold + fnam)
    coords = read_photo_coords(props)
    propsdict = process_photo_props(props, fnam, infold)

elif fn == 'note':
    fnam = input("Please enter a name for this note: ")
    datatype = "note";
    text = input("Please enter the note here: ")
    propsdict = {"name": fnam, "datetime": str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')), "datatype": "note", "text": text}
    declon = input("Please enter the longitude, in decimal degrees: ")
    declat = input("Please enter the latitude, in decimal degrees: ")
    alt = input("Please enter the altitude")
    coords = [float(declon), float(declat), float(alt)]

elif fn == 'meas':
    fnam = input("Please enter a name for this measurement: ")
    datatype = "meas"
    spec = input("Please enter what you are measuring here: ")
    text = float(input("Please enter the measurement value here: "))
    units = input("Please enter the measurement units here: ")
    propsdict = {"name": fnam, "datetime": str(datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')), "datatype": datatype, "species": spec, "value": text, "units": units}
    declon = input("Please enter the longitude, in decimal degrees: ")
    declat = input("Please enter the latitude, in decimal degrees: ")
    alt = input("Please enter the altitude")
    coords = [float(declon), float(declat), float(alt)]

if jsonnam not in listdir(outfold):
    with open(outfold + jsonnam, 'w+') as f:
        tags = []
        while True:
            tagin = input("Please enter tags for this item. Enter 'c' when finished: ")
            if tagin == 'c':
                break
            else:
                tags.append(tagin)

        tagjoin = ";".join(tags)

        propsdict['tags'] = tagjoin

        institList = ['public', 'institution', 'personal']
        for i, v in enumerate(institList):
            print(i, v)

        fn = input('Please select an access level from the list (type the number or name): ')
        try:
            fn = int(fn)
        except ValueError:
            fn = fn

        if type(fn) is int:
            if fn < institList.count:
                access = institList[fn]
            else:
                fn = input('Please select an access level from the list (type the number or name): ')
        elif type(fn) is str:
            if fn in institList:
                access = fn
            else:
                fn = input('Please select an access level from the list (type the number or name): ')

        propsdict['access'] = access

        featline = {"type": "Feature",
                    "properties": propsdict,
                    "geometry": {"type": "Point", "coordinates": coords}}

        featlist = []
        featline["id"] = random.randint(0, 10000)
        featlist.append(featline)
        dicto = {"type": "FeatureCollection", "features": featlist}
        json.dump(dicto, f, indent=1)
else:
    with open(outfold + jsonnam, 'r+') as f:
        read = json.load(f)
        feats = read['features']
        featid = None
        for feat in feats:
            nam = feat['properties']['name']
            readtags = feat['properties']['tags']
            if nam == fnam:
                readtagsSplit = readtags.split(";")
                featid = feat['id']
                newtags = []
                while True:
                    tagsin = input(
                        "Current tags: " + ", ".join(readtagsSplit) + ". Add additional tags or type 'c' to stop: ")
                    if tagsin == 'c':
                        break
                    else:
                        if tagsin not in readtagsSplit:
                            newtags.append(tagsin)
                break
            else:
                pass
        if featid is None:
            tags = []
            while True:
                tagin = input("Please enter tags for this item. Enter 'c' when finished: ")
                if tagin == 'c':
                    break
                else:
                    tags.append(tagin)

            tagjoin = ";".join(tags)

            propsdict['tags'] = tagjoin

            institList = ['public', 'institution', 'personal']
            for i, v in enumerate(institList):
                print(i, v)

            fn = input('Please select an access level from the list (type the number or name): ')
            try:
                fn = int(fn)
            except ValueError:
                fn = fn

            if type(fn) is int:
                if fn < len(institList):
                    access = institList[fn]
                else:
                    fn = input('Please select an access level from the list (type the number or name): ')
            elif type(fn) is str:
                if fn in institList:
                    access = fn
                else:
                    fn = input('Please select an access level from the list (type the number or name): ')

            propsdict['access'] = access

            featline = {"type": "Feature",
                        "properties": propsdict,
                        "geometry": {"type": "Point", "coordinates": coords}}

            newid = random.randint(0, 10000)
            while len([i for i, ltr in enumerate(feats) if ltr['id'] == newid]) > 0:
                newid = random.randint(0, 10000)
            featline["id"] = newid
            read['features'].append(featline)
            f.seek(0, 0)
            json.dump(read, f, indent=1)
        else:
            foundind = [i for i, ltr in enumerate(feats) if ltr['id'] == featid][0]
            if len(newtags) > 0:
                feats[foundind]['properties']['tags'] = readtags + ";" + ";".join(newtags)
            read['features'] = feats
            f.seek(0, 0)
            json.dump(read, f, indent=1)