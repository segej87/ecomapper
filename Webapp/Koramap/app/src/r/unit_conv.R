unit_conv <- function(vals,units,conversions,target) {
  library(parallel)
  
  myfun <- function(x) {
    eval(parse(text=x))
  }
  
  if (length(vals) < 8000) {
    op <- 1
  } else {
    op <- 2
  }
  
  if (op==1) {
    outVect <- c()
    for (i in 1:length(vals)) {
      if (units[i] == target) {
        outVect <- append(outVect,vals[i])
        next
      }
      
      text <- NA
      if (units[i] %in% names(conversions)) {
        if (conversions[[units[i]]] %in% c('x','x*1','x/1')) {
          outVect <- append(outVect,vals[i])
          next
        }
        text <- gsub('x',vals[i],conversions[[units[i]]])
        res <- as.numeric(myfun(text))
        outVect <- append(outVect,res)
      } else {
        outVect <- append(outVect,NA)
      }
    }
    return(outVect)
  } else {
    cat('Using cluster')
    cl <- makePSOCKcluster(detectCores())
    text <- rep(NA,length(vals))
    for (i in 1:length(vals)) {
      if (units[i] == target) {
        text[i] <- vals[i]
        next
      }
      if (units[i] %in% names(conversions)) {
        if (conversions[[units[i]]] %in% c('x','x*1','x/1')) {
          text[i] <- vals[i]
          next
        }
        text[i] <- gsub('x',vals[i],conversions[[units[i]]])
      }
    }
    outVect <- unlist(parSapply(cl=cl,X=text,FUN=myfun,USE.NAMES=F))
    stopCluster(cl)
    return(outVect)
  } 
}

res2 <- system.time(unit_conv(vals,units,conversions,'ppm'))
