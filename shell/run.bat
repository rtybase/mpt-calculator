rem "Standard Life data"
ParseTable.exe "-link=https://funds.standardlifeinvestments.com/uk-ifa/filter.html?search_type=pension&share_type=PS3" -format=CSV
del *.tmp
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeCsvDataTask -file=out.csv -outfile=std-life.csv
del out.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://secure.standardlife.co.uk/secure/fundfilter/rest/results/funds/GROUP_PENSIONS/GPP/null/existingcustomer?_=1601483194896" -outfile=std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformStdLifeJsonDataTask -file=std-life.json -outfile=std-life.csv
del std-life.json
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=std-life.csv
del std-life.csv

rem "GOLD prices"
ParseTable.exe "-link=http://lbma.datanauts.co.uk/table?metal=gold&year=2021&type=daily" -format=CSV
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

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=HSBA.L" "-out_symbol=HSBC" -outfile=t-HSBAL.csv
del "HSBC.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-HSBAL.csv
del t-HSBAL.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=SLA.L" "-out_symbol=Standard Life Aberdeen" -outfile=t-SLA.csv
del "Standard Life Aberdeen.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-SLA.csv
del t-SLA.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=BTC-USD" "-out_symbol=BITCOIN" -outfile=t-BITCOIN.csv
del "BITCOIN.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-BITCOIN.csv
del t-BITCOIN.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=PSN.L" "-out_symbol=Persimmon" -outfile=t-PSNL.csv
del "Persimmon.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-PSNL.csv
del t-PSNL.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=AV.L" "-out_symbol=Aviva" -outfile=t-AVL.csv
del "Aviva.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-AVL.csv
del t-AVL.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=BT-A.L" "-out_symbol=BT Group" -outfile=t-BT-AL.csv
del "BT Group.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-BT-AL.csv
del t-BT-AL.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=T" "-out_symbol=AT&T" -outfile=t-ATandT.csv
del "AT&T.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-ATandT.csv
del t-ATandT.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=CHL" "-out_symbol=China Mobile" -outfile=t-ChinaMobile.csv
del "China Mobile.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-ChinaMobile.csv
del t-ChinaMobile.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=HRB" "-out_symbol=H&R Block" -outfile=t-HandRBlock.csv
del "H&R Block.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-HandRBlock.csv
del t-HandRBlock.csv

java -jar portfolio-0.0.1-SNAPSHOT.jar TransformYFDataTask "-in_symbol=IRM" "-out_symbol=Iron Mountain" -outfile=t-IronMountain.csv
del "Iron Mountain.csv"
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=t-IronMountain.csv
del t-IronMountain.csv

rem "Vanguard data"
java -jar portfolio-0.0.1-SNAPSHOT.jar DownloadTask "-url=https://api.vanguard.com/rs/gre/gra/1.7.0/datasets/urd-product-list.jsonp?callback=angular.callbacks._5" -outfile=vanguard.json
java -jar portfolio-0.0.1-SNAPSHOT.jar TransformVanguardDataTask -file=vanguard.json -outfile=vanguard.csv
del vanguard.json
java -jar portfolio-0.0.1-SNAPSHOT.jar LoadCsvToDbTask -file=vanguard.csv
del vanguard.csv

java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar CalculateAssetStatsTask
java -Xmx512m -jar portfolio-0.0.1-SNAPSHOT.jar Calculate2AssetsPortfolioStatsTask
