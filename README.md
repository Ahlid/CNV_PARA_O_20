## CNV_PARA_O_20
##
### Instructions

Compile everything:
```bash
$ make all
```
Individual Modules: (some may have dependencies)
Compile __mazerunner__
```bash
$ make maze
```
Compile __bit__
```bash
$ make bit
```
Compile __loadbalancer__
```bash
$ make loadbalancer
```
Compile __storage__
```bash
$ make storage
```
Compile __httpserver__
```bash
$ make httpserver
```
Compile __instrumentation__
```bash
$ make instrumentation
```
___

Run __httpserver__:
```bash
$ make run_httpserver
```
___

Clean everything:
```bash
$ make clean
```
___

Refresh env:
```bash
$ make refresh
```
___

__Instrument__ mazerunner:
```bash
$ make inst_test
```

run samples bit:
java -cp "bin/:lib/*.jar" StatisticsTool -dynamic bin/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/ bin/pt/ulisboa/tecnico/meic/cnv/mazerunner/maze/
