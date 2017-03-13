library(parallel)

minLen <- 10
maxLen <- 500000
lenOut <- 10
trials <- 3
wNext <- T

testVect <- seq(from=minLen,to=maxLen,length.out=lenOut)

unitOps <- c('ppm','ppb','ppt','mg/l','g/l','mg/ml','ppq')

conversions <- list('ppm'='x',
                    'ppb'='x/1000',
                    'ppt'='x/1000000',
                    'mg/l'='x',
                    'g/l'='x/1000',
                    'mg/ml'='x/1000')
target <- 'ppm'

sockStart <- Sys.time()
cl <- makePSOCKcluster(detectCores())
sockEnd <- Sys.time()
sockElap <- as.numeric(sockEnd-sockStart)*1000
myfun <- function(x) eval(parse(text=x))

op1func <- function() {
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
    cat(paste(i,'\n',sep=""))
  }
}

op2func <- function() {
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
}

op1 <- c()
op2 <- c()
for (t in testVect) {
  vals <- runif(0:(t-1),min=0,max=100)
  units <- c()
  while (length(units) < length(vals)) {
    units <- append(units,unitOps[sample(1:length(unitOps),t,replace=T)])
  }
  
  for (op in 1:2) {
    for (m in 1:trials) {
      if (op==1) {
        op1 <- append(op1,system.time(op1func())[3])
      } else {
        op2 <- append(op2,system.time(op2func())[3])
      }
    }
  }
}

stopCluster(cl)

op2Clust <- op2Trim*1000+sockElap

lensVect <- rep(testVect,each=trials)
yStart <- 'op1Trim'
yNext <- 'op2Trim'
if (max(op2) > max(op1)) {
  yStart <- 'op2Trim'
  yNext <- 'op1Trim'
}
plot(x=lensVectTrim,y=get(yStart)*1000,xlab='Measurements (#)',ylab='Processing time (ms)')
points(x=lensVectTrim,y=get(yNext)*1000,col='red')
points(x=lensVectTrim,y=op2Clust,col='blue')
legend(x='topleft',legend=c(yStart,yNext,'op2+cl setup'),fill=c('black','red','blue'))

lm1 <- lm(formula=op1Trim*1000~lensVectTrim)
lm2 <- lm(formula=op2Clust~lensVectTrim)

abline(lm1)
abline(lm2)
