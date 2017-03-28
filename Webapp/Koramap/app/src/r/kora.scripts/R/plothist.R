plothist <- function (allVals, selVal, species, units) {
  par(bg='transparent', mar=c(5,2,4,2))

  svg(filename='out.svg', bg='transparent',width=4,height=3)

  hist <- hist(allVals, plot=F)
  plot(hist, xlab=paste('Values (',units,')',sep=""), main=paste('Histogram of',species), col='#b0e0e6',
       cex.axis=1, cex.main=1.5)
  abline(v=selVal, col=rgb(red=1,green=0.2941176,blue=0.3921569,alpha=0.9), lwd=2)

  dev.off()

  invisible()
}
