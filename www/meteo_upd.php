<?php

$db_host = 'localhost';
$db_name = 'weather';
$db_user = 'weather';
$db_pass = 'toys';


$RRDTOOL = "/usr/bin/rrdtool";
$DIR = "/var/lib/rrd/1-wire";
$dir = "/var/www/html/weather";
$edir = "/var/www/html/en/weather";
$RCFILE = "$dir/RainCounter";

header("Content-type: text/plain");

$temp2 = $wspd = $wdir = $temp1 = $temp3 = $temp4 = $temp5 = $wspdpk = "U";
$temp21 = $temp22 = $temp23 = $temp24 = $temp25 = "U";
$humidity = $rainin = $dailyrainin = $raincnt = $pressure = $dewpoint = "U";
$solar = "U";
$ts = $mn = "U";
$id = isset($_GET['ID']) ? $_GET['ID'] : 'home';
$pressure = isset($_GET['baromin']) ? $_GET['baromin'] : 'U';
$dewpoint = isset($_GET['dewptf']) ? $_GET['dewptf'] : 'U';

foreach ($_GET as $name => $value) {
    $$name = $value === 'error' ? 'U' : $value;
}

$uts = $ts;

//#
//#	read rainCounter.txt
//#
$rcd = $raincnt === 'U' ? 'U' : dailyrain($raincnt);
$rcw = $raincnt === 'U' ? 'U' : weeklyrain($raincnt);
$rcm = $raincnt === 'U' ? 'U' : monthlyrain($raincnt);
$rcy = $raincnt === 'U' ? 'U' : yearlyrain($raincnt);

$rainmm = $rainin === 'U' ? 'U' : sprintf("%.2f", $rainin*33.4);
$raincntmm = $raincnt === 'U' ? 'U' : sprintf("%.2f", $raincnt*33.4);
if (($rcd === 'U') || ($raincnt === 'U')) {
    $dailyrainmm = 'U';
    $weeklyrainmm = 'U';
    $monthlyrainmm = 'U';
    $yearlyrainmm = 'U';
} else {
    $dailyrainmm = sprintf("%.2f", ($raincnt - $rcd)*33.4);
	$weeklyrainmm = sprintf("%.2f", ($raincnt - $rcw)*33.4);
	$monthlyrainmm = sprintf("%.2f", ($raincnt - $rcm)*33.4);
	$yearlyrainmm = sprintf("%.2f", ($raincnt - $rcy)*33.4);
}
$presshpa = $pressure === 'U' ? 'U' : sprintf("%.0f", $pressure*33.864);
$pressmm = $pressure === 'U' ? 'U' : sprintf("%.0f", $pressure*25.4);
$humidity = $humidity === 'U' ? 'U' : sprintf("%.0f", $humidity);
$solar = $solar === 'U' ? 'U' : sprintf("%.0f", $solar);
if (($solar > 100) || ($solar < 0)) {
    $solar = 'U';
};
if ($humidity > 100) {
    $humidity = 100;
}

$temp = sprintf("%.1f", $temp2 - 0.57);
$dewpoint = sprintf("%.1f", $dewpoint - 0.57);

if ($id !== 'home') {
    rrd_update("$DIR/$id/weather.rrd",
        array("$ts:$temp21:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk"));
    echo "success $ts:$temp3:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk\n";
    log("$id:$ts1:$temp3:$low:$high:$secs:$cnt");
    exit();
}

if ($temp21 !== 'U') {
    rrd_update("$DIR/camel/weather.rrd",
        array("$ts:$temp21:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk"));
}
rrd_update("$DIR/wind.rrd", array("$ts:$temp1:$wspd:$wspdpk:$wdir"));

rrd_update("$DIR/temp2.rrd", array("$ts:$temp2"));
rrd_update("$DIR/temp3.rrd", array("$ts:$temp3"));
rrd_update("$DIR/rumb.rrd", array("N:22.5"));
rrd_update("DIR/rest.rrd", array("$ts:$temp4:$dewpoint:$humidity:$rainin:$dailyrainin:$pressure"));
rrd_update("$DIR/home/weather.rrd",
    array("$ts:$temp:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk"));
rrd_update("DIR/td/weather.rrd",
    array("$ts:$temp:$dewpoint:$humidity:$rainmm:$presshpa:$wdir:$wspd:$wspdpk:$solar"));
rrd_update("$DIR/solar.rrd", array("$ts:$solar"));
rrd_update("DIR/heater.rrd", array("$ts:$temp3:$low:$high:$secs:$cnt"));
rrd_update("$DIR/b1.rrd", array("$ts:$temp23:$low:$high"));
$b1der = 10*$temp23;
rrd_update("DIR/b1der.rrd", array("$ts:$b1der"));
rrd_update("$DIR/heater2.rrd", array("$ts:$temp3:$temp22:$low:$high:$secs:$cnt"));
echo "success\n";
log("$ts:$temp2:$dewpoint:$humidity:$rain:$dailyrain:$presshpa:$wdir:$wspd:$wspdpk");

//
//	MySQL update
//
//$dbh = DBI->connect("DBI:mysql:$db_name:localhost:3306", $db_user, $db_pass);

try {
    $conn = new \mysqli($db_host, $db_user, $db_pass, $db_name);
} catch (\Exception $e) {
    $conn = false;
    log("Could not connect to MySQL: " . $e->getMessage());
    exit(1);
}

$ts = $uts === 'U' ? "NULL" : $uts;
$temp = $temp2 === 'U' ? "NULL" : sprintf("%.1f", $temp2 - 0.57);
$pressure = $pressure === 'U' ? "NULL" : sprintf("%.0f", $pressure * 25.4);
$humidity = $humidity === 'U' ? "NULL" : $humidity;
$wspd = $wspd === 'U' ? "NULL" : $wspd;
$wspdpk = $wspdpk === 'U' ? "NULL" : $wspdpk;
$wdir = $wdir === 'U' ? "NULL" : $wdir;
$raincnt = $raincnt === 'U' ? "NULL" : $raincnt;
$solar = $solar === 'U' ? "NULL" : $solar;

$sql = "INSERT INTO meteo_data(ts, temp, pressure, humidity, 
	wind_dir, wind_spd, wind_gust, rain, station_id, solar) 
	VALUES ($ts, $temp, $pressure, $humidity,
	$wdir, $wspd, $wspdpk, $raincnt, '$id', $solar)";

$res = $conn->query($sql);

if (!$res) {
    log("Could not insert row to MySQL");
    exit(1);
}

function log($msg)
{
    file_put_contents('/home/boris/WeatherToys/mysw/meteo_upd.log',
        date("ymd-Hi ") . $msg . "\n", FILE_APPEND);
}

function dailyrain($rc)
{
    global $RCFILE;

    $dd = date('d');
    if ($str = file_get_contents($RCFILE . '-d.txt')) {
        $day = explode(',', $str)[0];
        if ($dd === $day) { // data exists
            return;
        }
    }
    file_put_contents($RCFILE . '-d.txt', $dd . ',' . $rc);
}

function weeklyrain($rc)
{
    global $RCFILE;

    $dd = date('W');
    if ($str = file_get_contents($RCFILE . '-w.txt')) {
        $day = explode(',', $str)[0];
        if ($dd === $day) { // data exists
            return;
        }
    }
    file_put_contents($RCFILE . '-w.txt', $dd . ',' . $rc);
}

function monthlyrain($rc)
{
    global $RCFILE;

    $dd = date('m');
    if ($str = file_get_contents($RCFILE . '-m.txt')) {
        $day = explode(',', $str)[0];
        if ($dd === $day) { // data exists
            return;
        }
    }
    file_put_contents($RCFILE . '-m.txt', $dd . ',' . $rc);
}

function yearlyrain($rc)
{
    global $RCFILE;

    $dd = date('y');
    if ($str = file_get_contents($RCFILE . '-y.txt')) {
        $day = explode(',', $str)[0];
        if ($dd === $day) { // data exists
            return;
        }
    }
    file_put_contents($RCFILE . '-y.txt', $dd . ',' . $rc);
}

