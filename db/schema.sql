create database portfolio;
use portfolio;

create table tbl_assets (
	int_assetID int(10) unsigned NOT NULL auto_increment,
	vchr_name varchar(100) not null,
	vchr_symbol varchar(50),
	vchr_price_symbol varchar(50),
	vchr_type VARCHAR(20) NOT NULL default "Stock",
	primary key (int_assetID),
	unique (vchr_name)
) ENGINE = InnoDB;

CREATE UNIQUE INDEX idx_tbl_assets_symbol
ON tbl_assets (vchr_symbol);

CREATE INDEX idx_tbl_assets_assetID_name
ON tbl_assets (int_assetID, vchr_name);

CREATE INDEX idx_tbl_assets_assetID_name_symbol_type
ON tbl_assets (int_assetID, vchr_name, vchr_symbol, vchr_type);

create table tbl_sectors (
	int_sectorID int(10) unsigned NOT NULL auto_increment,
	vchr_name varchar(100) not null,
	primary key (int_sectorID),
	unique (vchr_name)
) ENGINE = InnoDB;

create table tbl_industries (
	int_industryID int(10) unsigned NOT NULL auto_increment,
	vchr_name varchar(100) not null,
	primary key (int_industryID),
	unique (vchr_name)
) ENGINE = InnoDB;

create table tbl_stocks (
	vchr_symbol varchar(50) not null,
	fk_sectorID int(10) unsigned NOT NULL,
	fk_industryID int(10) unsigned NOT NULL,
	primary key (vchr_symbol),
	foreign key(fk_sectorID) references tbl_sectors (int_sectorID),
	foreign key(fk_industryID) references tbl_industries (int_industryID)
) ENGINE = InnoDB;

create table tbl_fscores (
	vchr_symbol varchar(50) not null,
	dtm_date DATE NOT NULL,
	dbl_fscore DOUBLE not null,
	primary key (vchr_symbol, dtm_date),
	foreign key(vchr_symbol) references tbl_stocks (vchr_symbol)
) ENGINE = InnoDB;

create table tbl_finances_quarter (
	vchr_symbol varchar(50) NOT NULL,
	dtm_date DATE NOT NULL,
	dbl_total_current_assets DOUBLE,
	dbl_total_current_liabilities DOUBLE,
	dbl_total_assets DOUBLE,
	dbl_total_liabilities DOUBLE,
	dbl_total_equity DOUBLE,
	dbl_net_cash_flow_operating DOUBLE,
	dbl_capital_expenditures DOUBLE,
	primary key (vchr_symbol, dtm_date),
	foreign key(vchr_symbol) references tbl_stocks (vchr_symbol)
) ENGINE = InnoDB;

create table tbl_prices (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_price DOUBLE not null,
	dbl_change DOUBLE not null,
	dbl_return DOUBLE not null,
	dbl_volume DOUBLE,
	dbl_vol_change_rate DOUBLE,
	dtm_date DATE not null, -- dd-mm-yy
	dtm_time TIME not null, -- hh:mm:ss
	primary key(fk_assetID, dtm_date, dtm_time),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

CREATE INDEX idx_prices_id_date
ON tbl_prices (fk_assetID, dtm_date);

create table tbl_dividends (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_pay DOUBLE not null,
	dtm_date DATE not null, -- dd-mm-yy
	primary key(fk_assetID, dtm_date),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_eps (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_eps DOUBLE not null,
	dbl_prd_eps DOUBLE,
	int_no_of_analysts INT UNSIGNED,
	dtm_date DATE not null, -- dd-mm-yy
	primary key(fk_assetID, dtm_date),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_n_gaap_eps (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_eps DOUBLE not null,
	dbl_prd_eps DOUBLE,
	dtm_date DATE not null, -- dd-mm-yy
	bln_after_market_close BOOLEAN not null,
	dbl_revenue DOUBLE,
	dbl_prd_revenue DOUBLE,
	primary key(fk_assetID, dtm_date),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_earnings (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_eps DOUBLE not null,
	dtm_date DATE not null, -- dd-mm-yy
	primary key(fk_assetID, dtm_date),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_predictions (
	fk_assetID int(10) unsigned NOT NULL,
	vchr_model varchar(50) not null,
	dtm_eps_date DATE not null, -- dd-mm-yy
	int_days_after_eps int(10) unsigned NOT NULL,
	dtm_prd_date DATE not null, -- dd-mm-yy
	dbl_prd_return DOUBLE not null,
	primary key(fk_assetID, vchr_model, dtm_eps_date, int_days_after_eps),
	foreign key(fk_assetID) references tbl_assets (int_assetID),
	unique (fk_assetID, vchr_model, dtm_prd_date)
) ENGINE = InnoDB;

create table tbl_avgreturns (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_1w (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_1m (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_6m (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_1y (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_2y (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_avgreturns_5y (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_avgreturn DOUBLE not null,
	dbl_varience DOUBLE not null,
	primary key(fk_assetID),
	foreign key(fk_assetID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

create table tbl_correlations (
	fk_asset1ID int(10) unsigned NOT NULL,
	fk_asset2ID int(10) unsigned NOT NULL,
	dbl_covariance DOUBLE not null,
	dbl_correlation DOUBLE not null,
	dbl_weight1 DOUBLE not null,
	dbl_weight2 DOUBLE not null,
	dbl_portret DOUBLE not null,
	dbl_portvar DOUBLE not null,
	primary key(fk_asset1ID,fk_asset2ID),
	foreign key(fk_asset1ID) references tbl_assets (int_assetID),
	foreign key(fk_asset2ID) references tbl_assets (int_assetID)
) ENGINE = InnoDB;

CREATE INDEX idx_correlations_asset2ID_portret
ON tbl_correlations (fk_asset2ID, dbl_portret);

create table tbl_shift_correlations (
	fk_asset1ID int(10) unsigned NOT NULL,
	fk_asset2ID int(10) unsigned NOT NULL,
	int_shift int(10) NOT NULL,
	dbl_correlation DOUBLE NOT NULL,
	int_continuous_updates int(10) NOT NULL default 0,
	dtm_last_update_date DATE NOT NULL,
	txt_json text NOT NULL,
	primary key(fk_asset1ID,fk_asset2ID),
	foreign key(fk_asset1ID) references tbl_assets (int_assetID),
	foreign key(fk_asset2ID) references tbl_assets (int_assetID)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

CREATE INDEX idx_shift_correlations_shift
ON tbl_shift_correlations (int_shift);

create table tbl_custom_portfolios (
	int_portfolioID int(10) unsigned NOT NULL auto_increment,
	vchr_name varchar(100) not null,
	txt_json_composition text NOT NULL,
	primary key (int_portfolioID),
	unique (vchr_name)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

create table tbl_custom_portfolios_data (
	fk_portfolioID int(10) unsigned NOT NULL,
	dtm_date DATE not null, -- dd-mm-yy
	txt_json_stats text NOT NULL,
	primary key (fk_portfolioID,dtm_date),
	foreign key(fk_portfolioID) references tbl_custom_portfolios (int_portfolioID)
) ENGINE = InnoDB DEFAULT CHARSET=utf8;

create table tbl_ml_quality (
	int_dataset int(10) unsigned NOT NULL,
	vchr_metric varchar(50)  NOT NULL,
	vchr_model varchar(50)  NOT NULL,
	dtm_report_date DATE NOT NULL,
	dbl_result DOUBLE NOT NULL,
	bln_after_retrain BOOLEAN NOT NULL,
	primary key(int_dataset, vchr_metric, vchr_model, dtm_report_date)
) ENGINE = InnoDB;


----------------
delimiter //

CREATE PROCEDURE usp_addPrice (IN asset VARCHAR(100),
		IN price DOUBLE, IN price_change DOUBLE,
		IN roi DOUBLE,
		IN regDate DATE, IN regTime TIME,
		OUT result INT)
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		SET result = -1; -- Means ERROR
	END;

	SET result = -1; -- Means error occured
	SELECT int_assetID INTO result FROM tbl_assets WHERE UPPER(vchr_name)=UPPER(asset);

	IF (result > 0) THEN
		START TRANSACTION;
			INSERT INTO tbl_prices (fk_assetID, dbl_price, dbl_change,
				dbl_return, dtm_date, dtm_time)
			VALUES (result, price, price_change, 
				roi, regDate, regTime)
			ON DUPLICATE KEY UPDATE 
				dbl_price = price,
				dbl_change = price_change,
				dbl_return = roi;
		COMMIT;
	END IF;
END;
//

delimiter ;

delimiter //

CREATE PROCEDURE usp_applyavg (OUT result INT)
BEGIN
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
	BEGIN
		ROLLBACK;
		SET result = -1; -- Means ERROR
	END;

	START TRANSACTION;

	DELETE FROM tbl_avgreturns;
	DELETE FROM tbl_avgreturns_1w;
	DELETE FROM tbl_avgreturns_1m;
	DELETE FROM tbl_avgreturns_6m;
	DELETE FROM tbl_avgreturns_1y;
	DELETE FROM tbl_avgreturns_2y;
	DELETE FROM tbl_avgreturns_5y;

	INSERT INTO tbl_avgreturns (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_1w (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 1 WEEK) AND NOW()
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_1m (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 1 MONTH) AND NOW()
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_6m (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 6 MONTH) AND NOW()
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_1y (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 1 YEAR) AND NOW()
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_2y (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 2 YEAR) AND NOW()
	GROUP BY fk_assetID;

	INSERT INTO tbl_avgreturns_5y (fk_assetID, dbl_avgreturn, dbl_varience)
	SELECT fk_assetID, AVG(dbl_return), VAR_POP(dbl_return)
	FROM tbl_prices
	WHERE dtm_date BETWEEN (NOW() - INTERVAL 5 YEAR) AND NOW()
	GROUP BY fk_assetID;

	COMMIT;

	SET result = 1;
END;
//

delimiter ;
