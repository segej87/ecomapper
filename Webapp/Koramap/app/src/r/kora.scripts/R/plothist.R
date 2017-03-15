plothist <- function (allVals, selVal, species, unit) {
  hist <- hist(allVals, plot=F)
  plot(hist, xlab=paste('Values (',unit,')',sep=""), main=paste('Histogram of',species))
  abline(v=selVal, col='blue', lwd=2)

  invisible()
}
