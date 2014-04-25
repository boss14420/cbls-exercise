## Graph Coloring

### Biên dịch

    make

hoặc

    java -cp .:open-localsearch-20140421-2.jar GraphColoring.java

### Chạy chương trình

    ./graph-coloring.sh FILENAME [NUMTEST]

hoặc

    java -cp .:open-localsearch-20140421-2.jar GraphColoring FILENAME [NUMTEST]

Với:

1. FILENAME: file dữ liệu đồ thị, nằm trong thư mục *graph_color*. (Nguồn:
http://www.cs.hbg.psu.edu/txn131/graphcoloring.html)
2. NUMTEST: số lần chạy bộ dữ liệu trên. In ra kết quả mỗi lần chạy và `min`, `max`, `mean`, `stddev`. Nếu không có hoặc bằng 0 thì chỉ chạy một lần và in ra từng bước của chương trình.
