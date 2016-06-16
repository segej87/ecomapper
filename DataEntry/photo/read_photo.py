from exifread import process_file
import datetime

def find(s, ch):
    return [i for i, ltr in enumerate(s) if ltr == ch]

def read_all_props(props):
    for tag in props.keys():
       if tag not in ('JPEGThumbnail', 'TIFFThumbnail', 'Filename', 'EXIF MakerNote'):
           print("Key: %s, Value: %s" % (tag, props[tag]))

def read_photo_props(fp):
    with open(fp, 'rb') as f:
        return process_file(f)


def read_photo_coords(props):
    try:
        lat = str(props['GPS GPSLatitude'])
        latdelim = find(lat, ',')

        latdeg = float(lat[1:latdelim[0]])
        latmin = float(lat[(latdelim[0] + 2):latdelim[1]])
        latsec = lat[(latdelim[1] + 2):str.find(lat, ']')]

        if str.find(latsec, '/') != None:
            latsecnum = int(latsec[0:str.find(latsec, '/')])
            latsecden = int(latsec[(str.find(latsec, '/') + 1):len(latsec)])
            latsec = float(latsecnum) / float(latsecden)
        else:
            latsec = float(latsec)

        declat = latdeg + float(latmin) / 60 + latsec / 3600

        if str(props['GPS GPSLatitudeRef']) == 'S':
            declat = -declat

        # Get longitude from photo, convert to decimal
        lon = str(props['GPS GPSLongitude'])
        lonref = str(props['GPS GPSLongitudeRef'])
        londelim = find(lon, ',')

        londeg = float(lon[1:londelim[0]])
        lonmin = float(lon[(londelim[0] + 2):londelim[1]])
        lonsec = lon[(londelim[1] + 2):str.find(lon, ']')]

        if str.find(lonsec, '/') != None:
            lonsecnum = float(lonsec[:str.find(lonsec, '/')])
            lonsecden = float(lonsec[(str.find(lonsec, '/') + 1):])
            lonsec = lonsecnum / lonsecden
        else:
            lonsec = float(lonsec)

        declon = londeg + float(lonmin) / 60 + lonsec / 3600
        if lonref == 'W':
            declon = - declon

        # Get altitude from photo, convert to decimal
        alt = str(props['GPS GPSAltitude'])
        if str.find(alt, '/') >= 0:
            altnum = float(alt[:str.find(alt, '/')])
            altden = float(alt[(str.find(alt, '/') + 1):])
            alt = altnum / altden
        else:
            alt = float(alt)

        print('Congrats, this photo is geotagged')

        return [declon, declat, alt]
    except KeyError:
        print('Error: geolocation not found. Would you like to search?')

def process_photo_props(props, fnam, infold):
    try:
        datatype = "photo"

        if 'EXIF DateTimeOriginal' in props.keys():
            fileDate = str(datetime.datetime.strptime(str(props['EXIF DateTimeOriginal']), '%Y:%m:%d %H:%M:%S'))
        elif 'Image DateTime' in props.keys():
            fileDate = str(datetime.datetime.strptime(str(props['Image DateTime']), '%Y:%m:%d %H:%M:%S'))

        return {"name": fnam, "datetime": fileDate, "datatype": datatype,
                     "filepath": infold}
    except KeyError:
        print('Error: geolocation not found. Would you like to search?')