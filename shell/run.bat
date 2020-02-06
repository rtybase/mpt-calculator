rem "Standard Life data"
ParseTable.exe "-link=https://funds.standardlifeinvestments.com/uk-ifa/filter.html?search_type=pension&share_type=PS3" -format=CSV
del *.tmp
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeDataTask -file=out.csv -outfile=std-life.csv
del out.csv
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=std-life.csv
del std-life.csv

ParseTable.exe "-link=http://lbma.datanauts.co.uk/table?metal=gold&year=2019&type=daily" -format=CSV
del *.tmp
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformLbmaDataTask -file=out.csv -outfile=gold.csv -out_symbol=GOLD
del out.csv
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=gold.csv
del gold.csv

rem "Bank of England data"
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformBoEDataTask "-in_symbol=C8P" "-out_symbol=GPB/USD" -outfile=boe.csv
del C8P.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=boe.csv
del boe.csv

rem "European Central Bank data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=http://www.ecb.europa.eu/stats/eurofxref/eurofxref-hist-90d.xml" -outfile=ecb_rates.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformEcbRatesTask -file=ecb_rates.xml -outfile=ecb.csv
del ecb_rates.xml
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=ecb.csv
del ecb.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=%%5EFTSE" "-out_symbol=FTSE100" -outfile=t-FTSE100.csv
del FTSE100.csv
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-FTSE100.csv
del t-FTSE100.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=%%5EFCHI" "-out_symbol=CAC40" -outfile=t-CAC40.csv
del CAC40.csv
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-CAC40.csv
del t-CAC40.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=%%5ENDX" "-out_symbol=NASDAQ100" -outfile=t-NASDAQ100.csv
del NASDAQ100.csv
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-NASDAQ100.csv
del t-NASDAQ100.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=%%5EGSPC" "-out_symbol=S&P500" -outfile=t-SandP.csv
del "S&P500.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-SandP.csv
del t-SandP.csv

rem "Vanguard data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://api.vanguard.com/rs/gre/gra/1.7.0/datasets/urd-product-list.jsonp?callback=angular.callbacks._5" -outfile=vanguard.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformVanguardDataTask -file=vanguard.json -outfile=vanguard.csv
del vanguard.json
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=vanguard.csv
del vanguard.csv

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
