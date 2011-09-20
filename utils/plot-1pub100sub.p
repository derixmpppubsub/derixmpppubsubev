
# output as png image
set terminal png

# save file to "out.png"
set output "out.png"

# graph title
set title "1 pub, 100 sub"

# nicer aspect ratio for image size
set size 1,0.7

# y-axis grid
set grid y

# x-axis label
set xlabel "item"

# y-axis label
set ylabel "response time (nanosecs)"

set datafile separator ","

# plot data from "out.dat" using column 9 with smooth sbezier lines
# and title of "nodejs" for the given data
#plot "1pub100sub-results.csv" using 9 smooth sbezier with lines title "XMMP PubSub"

plot "1pub100sub-results.csv" 

#gnuplog plot.t