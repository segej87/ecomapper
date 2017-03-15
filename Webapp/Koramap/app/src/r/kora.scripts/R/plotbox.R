plotbox <- function (allVals, selVal, species) {
  boxplot(allVals, horizontal=F)
  points(x=selVal, col='blue', pch=8, lwd=2)

  invisible();
}
