create database portfolio;
use portfolio;

create table tbl_assets (
	int_assetID int(10) unsigned NOT NULL auto_increment,
	vchr_name varchar(100) not null,
	primary key (int_assetID),
	unique (vchr_name)
) ENGINE = InnoDB;

create table tbl_prices (
	fk_assetID int(10) unsigned NOT NULL,
	dbl_price DOUBLE not null,
	dbl_change DOUBLE not null,
	dbl_return DOUBLE not null,
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

CREATE INDEX idx_dividends_id_date
ON tbl_dividends (fk_assetID, dtm_date);

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

create table tbl_shift_correlations (
	fk_asset1ID int(10) unsigned NOT NULL,
	fk_asset2ID int(10) unsigned NOT NULL,
	int_shift int(10) NOT NULL,
	dbl_correlation DOUBLE not null,
	primary key(fk_asset1ID,fk_asset2ID),
	foreign key(fk_asset1ID) references tbl_assets (int_assetID),
	foreign key(fk_asset2ID) references tbl_assets (int_assetID)
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
	DELETE FROM tbl_correlations;
	DELETE FROM tbl_shift_correlations;

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
