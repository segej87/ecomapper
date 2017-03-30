shapeidw <- function (shapeString, x, y, z, n=50000, idp=2.0, alpha=0.5, save=F, guid=NA, filename=NA) {
  library(rgdal)
  library(gstat)
  library(sp)
  library(raster)
  library(httr)
  library(base64enc)

  shapejson <- jsonlite::fromJSON(shapeString)
  shapecoords <- shapejson$features$geometry$coordinates
  polys <- list()
  id <- 1

  if (shapejson$features$geometry$type == 'MultiPolygon') {
    for (i in 1:length(shapecoords)) {
      innerpolys <- list()
      for (j in 1:length(shapecoords[[i]])) {
        innerpolys <- append(innerpolys,Polygon(matrix(shapecoords[[i]][[j]],ncol = 2)))
      }
      polys <- append(polys,Polygons(innerpolys,id))

      id <- id + 1
    }
  } else if (shapejson$features$geometry$type == 'Polygon') {
    innerpolys <- list()
    innerpolys <- append(innerpolys,Polygon(matrix(shapecoords[[1]],ncol=2)))
    polys <- append(polys,Polygons(innerpolys,id))
  }

  shape <- SpatialPolygons(Srl=polys, proj4string=CRS("+proj=longlat +datum=WGS84 +no_defs +ellps=WGS84 +towgs84=0,0,0"))

  remVect <- which(is.na(z))
  if (length(remVect) > 0) {
    x <- x[-remVect]
    y <- y[-remVect]
    z <- z[-remVect]
  }
  point_data <- data.frame(x=x,y=y)
  point_data_sp <- SpatialPoints(point_data,proj4string = CRS(proj4string(shape)))

  # Create an empty grid where n is the total number of cells
  grd              <- as.data.frame(spsample(shape, "regular", n=n))
  names(grd)       <- c("X", "Y")
  coordinates(grd) <- c("X", "Y")
  gridded(grd)     <- TRUE  # Create SpatialPixel object
  fullgrid(grd)    <- TRUE  # Create SpatialGrid object

  # Add shape's projection information to the empty grid
  proj4string(grd) <- proj4string(shape)

  # Interpolate the grid cells using a power value of 2 (idp=2.0)
  shape.idw <- idw(z ~ 1, point_data_sp, newdata=grd, idp=idp, na.action=na.pass)

  # Convert to raster object then clip to shape
  r       <- raster(shape.idw)
  r.m     <- mask(r, shape)

  hist <- raster::hist(r.m,plot=F)
  breaks=length(hist$breaks)
  extent = bbox(r.m)
  w <- ncol(r.m)/max(dim(r.m))
  h <- nrow(r.m)/max(dim(r.m))

  my_palette <- colorRampPalette(c("green", "yellow", "red"))(n = breaks)

  png('out.png',width=480*w,height=480*h,bg='transparent')
  plot.new()
  par(mar=c(0,0,0,0), oma=c(0,0,0,0), xpd=NA)
  plot.window(xlim=extent(r.m)[1:2], ylim=extent(r.m)[3:4], xaxs="i",yaxs="i")
  raster::plot(r.m,axes=F,box=F,legend=F,breaks=hist$breaks,col=my_palette,alpha=alpha,add=T)
  dev.off()

  rastOut <- raster::writeRaster(r.m, filename, 'GTiff',overwrite=T)
  if (save) {
    url <- "http://ecocollector.azurewebsites.net/add_media.php"
    fileupload <- base64encode(paste(filename,'.tif',sep=""))
    body <- list('GUID'=guid,'filename'=paste('model',filename,gsub("-","_",Sys.Date()),sep="_"),'file'=fileupload)
    result <- httr::POST(url=url,body=body,encode="multipart")
  }

  invisible()
}
