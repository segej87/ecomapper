standardizeunits <- function(vals,units,conversions,target) {
  myfun <- function(x) {
    eval(parse(text=x))
  }

  outVect <- c()
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
  outVect <- sapply(X=text,FUN=myfun,USE.NAMES=F)
  return(outVect)
}
