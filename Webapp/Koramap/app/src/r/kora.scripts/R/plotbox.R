plotbox <- function (allVals, selVal, species, units) {
  par(bg='transparent', mar=c(5,2,4,2))

  svg(filename='out.svg', bg='transparent',width=4,height=3)
  boxplot(allVals, horizontal=T, frame=F, col='#b0e0e6', main=paste(species,' (',units,')',sep=""), pars=list(boxwex = 1, staplewex = 0.75, boxlwd = 2, staplelwd = 2, plot.frame=F, cex.axis=1, cex.main=1.5))
  points(x=selVal, y=order(selVal),col=rgb(red=1,green=0.2941176,blue=0.3921569,alpha=0.9), pch=8, lwd=2, cex=2)
  dev.off()
  invisible();
}
