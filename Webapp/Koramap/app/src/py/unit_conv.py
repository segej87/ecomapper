import numpy;

len = 10;
interval = 5;
trials = 3;
wNext = True;

vals = numpy.random.normal(1,0.2,100);
unitOps = ['ppm','ppb','ppt','mg/l','g/l','mg/l','ppq'];
valLen = len(vals);
valOps = len(unitOps);
ints = numpy.random.randint(0,7,100);
