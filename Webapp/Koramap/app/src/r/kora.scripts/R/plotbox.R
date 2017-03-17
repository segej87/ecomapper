plotbox <- function (allVals, selVal, species, units) {
  # library(ggplot2)
  #
  par(bg='transparent', mar=c(5,2,4,2))
  #
  # g <- ggplot(NULL, aes(1,allVals)) +
  #   scale_x_discrete(NULL, NULL, NULL, c("0.8","1.2"))+
  #   geom_boxplot() + geom_point(aes(x=1,y=selVal), color='blue', pch=8, cex=5, stroke=2) +
  #   ggtitle(paste("Distribution of",species)) +
  #   theme(plot.title=element_text(size=20, face="bold")) +
  #   theme(axis.text.x=element_text(size=14)) +
  #   ylab(paste("Values (",units,")",sep="")) +
  #   theme(axis.title.y=element_text(size=20))
  # last_plot() + coord_flip()

  svg(filename='out.svg', bg='transparent',width=4,height=3)
  boxplot(allVals, horizontal=T, frame=F, col='#b0e0e6', main=paste(species,' (',units,')',sep=""), pars=list(boxwex = 1, staplewex = 0.75, boxlwd = 2, staplelwd = 2, plot.frame=F, cex.axis=1, cex.main=1.5))
  points(x=selVal, y=order(selVal),col=rgb(red=1,green=0.2941176,blue=0.3921569,alpha=0.9), pch=8, lwd=2, cex=2)
  dev.off()
  invisible();
}
