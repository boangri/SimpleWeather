#!/usr/bin/perl
#
# $Id: meteo_upd,v 1.61 2010/01/06 19:20:16 boris Exp $
# This CGI script stores data into RDD and MySQL databases
#
use DBI;

$db_name = 'weather';
$db_user = 'weather';
$db_pass = 'toys';

$RRDTOOL = "/usr/bin/rrdtool";
$DIR = "/var/lib/rrd/1-wire";
$dir = "/var/www/html/weather";
$edir = "/var/www/html/en/weather";
$RCFILE = "$dir/RainCounter";

sub parse_template_array() {
        my ($page, $mar) = @_;
        for ($c = 0; $c <= $#$mar; $c++) {
                my $data = $mar->[$c][1];
                $page =~ s/%%$mar->[$c][0]%%/$$data/gsex;
        }
        return $page;
}

print "Content-type: text/plain\n\n";

$qstring = $ENV{'QUERY_STRING'};
@pairs = split(/&/, $qstring);
$temp2 = $wspd = $wdir = $temp1 = $temp3 = $temp4 = $temp5 = $wspdpk = "U";
$temp21 = $temp22 = $temp23 = $temp24 = $temp25 = "U";
$humidity = $rainin = $dailyrainin = $raincnt = $pressure = $dewpoint = "U";
$solar = "U";
$ts = $mn = "U";
$id = 'home';
foreach $pair (@pairs) {
  ($name, $value) = split(/=/, $pair);
  if ($name eq 'ID') {$id = $value;}
  if ($name eq 'ts') {$ts = $value;}
  if ($name eq 'mn') {$mn = $value;}
  if ($name eq 'temp1') {$temp1 = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'temp2') {$temp2 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp3') {$temp3 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp4') {$temp4 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp5') {$temp5 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp21') {$temp21 = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'temp22') {$temp22 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp23') {$temp23 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp24') {$temp24 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'temp25') {$temp25 = $value eq 'error' ? 'U' : $value;} 
  if ($name eq 'wspd') {$wspd = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'wspdpk') {$wspdpk = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'wdir') {$wdir = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'humidity') {$humidity = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'solar') {$solar = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'baromin') {$pressure = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'dewptf') {$dewpoint = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'rainin') {$rainin = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'raincnt') {$raincnt = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'secs') {$secs = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'cnt') {$cnt = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'low') {$low = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'high') {$high = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'temph') {$temph = $value eq 'error' ? 'U' : $value;}
  if ($name eq 'pulse') {$pulse = $value eq 'error' ? 'U' : $value;}
}
$uts = $ts;
#
#	read rainCounter.txt
#
$rcd = "$raincnt" eq 'U' ? 'U' : &dailyrain($raincnt);
$rcw = "$raincnt" eq 'U' ? 'U' : &weeklyrain($raincnt);
$rcm = "$raincnt" eq 'U' ? 'U' : &monthlyrain($raincnt);
$rcy = "$raincnt" eq 'U' ? 'U' : &yearlyrain($raincnt);
#
#	RRD update
#

$rainmm = $rainin eq 'U' ? 'U' : sprintf "%.2f", $rainin*33.4;
$raincntmm = $raincnt eq 'U' ? 'U' : sprintf "%.2f", $raincnt*33.4;
if (($rcd eq 'U') || ($raincnt eq 'U')) {
	$dailyrainmm = 'U';
	$weeklyrainmm = 'U';
	$monthlyrainmm = 'U';
	$yearlyrainmm = 'U';
} else {
	$dailyrainmm = sprintf "%.2f", ($raincnt - $rcd)*33.4;
	$weeklyrainmm = sprintf "%.2f", ($raincnt - $rcw)*33.4;
	$monthlyrainmm = sprintf "%.2f", ($raincnt - $rcm)*33.4;
	$yearlyrainmm = sprintf "%.2f", ($raincnt - $rcy)*33.4;
}
$presshpa = $pressure eq 'U' ? 'U' : sprintf "%.0f", $pressure*33.864;
$pressmm = $pressure eq 'U' ? 'U' : sprintf "%.0f", $pressure*25.4;
$humidity = $humidity eq 'U' ? 'U' : sprintf "%.0f", $humidity;
$solar = $solar eq 'U' ? 'U' : sprintf "%.0f", $solar;
if (($solar > 100) || ($solar < 0)) {$solar = 'U'};
if ($humidity > 100) { $humidity = 100; }

$temp = sprintf "%.1f", $temp2 - 0.57;
$dewpoint = sprintf "%.1f", $dewpoint - 0.57;

if ($id ne 'home') {
	`$RRDTOOL update $DIR/$id/weather.rrd $ts:$temp21:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk`;
	print "success $ts:$temp3:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk\n";
	open (LOG, ">>/home/boris/WeatherToys/mysw/meteo_upd.log");
	$ts1=`date +%y%m%d-%H%M`;
	chomp $ts1;
	print LOG "$id:$ts1:$temp3:$low:$high:$secs:$cnt\n";
	close LOG;
	exit;
}
if ($temp21 ne 'U') {
	`$RRDTOOL update $DIR/camel/weather.rrd $ts:$temp21:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk`;
}
`$RRDTOOL update $DIR/wind.rrd $ts:$temp1:$wspd:$wspdpk:$wdir`;
`$RRDTOOL update $DIR/temp2.rrd $ts:$temp2`;
`$RRDTOOL update $DIR/temp3.rrd $ts:$temp3`;
`$RRDTOOL update $DIR/rumb.rrd N:22.5`;
`$RRDTOOL update $DIR/rest.rrd $ts:$temp4:$dewpoint:$humidity:$rainin:$dailyrainin:$pressure`;
`$RRDTOOL update $DIR/home/weather.rrd $ts:$temp:$dewpoint:$humidity:$rainmm:$raincntmm:$presshpa:$wdir:$wspd:$wspdpk`;
`$RRDTOOL update $DIR/td/weather.rrd $ts:$temp:$dewpoint:$humidity:$rainmm:$presshpa:$wdir:$wspd:$wspdpk:$solar`;
`$RRDTOOL update $DIR/solar.rrd $ts:$solar`;
`$RRDTOOL update $DIR/heater.rrd $ts:$temp3:$low:$high:$secs:$cnt`;
`$RRDTOOL update $DIR/b1.rrd $ts:$temp23:$low:$high`;
$b1der = 10*$temp23;
`$RRDTOOL update $DIR/b1der.rrd $ts:$b1der`;
`$RRDTOOL update $DIR/heater2.rrd $ts:$temp3:$temp22:$low:$high:$secs:$cnt`;
print "success\n";

open (LOG, ">>/home/boris/WeatherToys/mysw/meteo_upd.log");
$ts=`date +%y%m%d-%H%M`;
chomp $ts;
print LOG "$ts:$temp2:$dewpoint:$humidity:$rain:$dailyrain:$presshpa:$wdir:$wspd:$wspdpk\n";
close LOG;

#
#	index HTML output
#
#$datestr = `date +"%F %H:%M"`;
$datestr = `date +"%Y/%m/%d %H:%M"`;
$ts = `date +"%Y/%m/%d %H:%M:%S"`;
chomp $datestr;
chomp $ts;
($date, $time) = split / /, $datestr;
$lats = "54&deg 45.72'N";
$lons = "37&deg 31.28'E";
$wspd = sprintf "%.1f", $wspd;
$wdir = sprintf "%.0f", $wdir;
$wspdpk = sprintf "%.1f", $wspdpk;

@parsearray = ();
push (@parsearray, ['DAT', \"$date"]);
push (@parsearray, ['TIM', \"$time"]);
push (@parsearray, ['LAT', \$lats]);
push (@parsearray, ['LON', \$lons]);
push (@parsearray, ['TEM', \$temp]);
push (@parsearray, ['BAR', \$pressmm]);
push (@parsearray, ['GPS', \$presshpa]);
push (@parsearray, ['HUM', \$humidity]);
push (@parsearray, ['WNS', \$wspd]);
push (@parsearray, ['WNG', \$wspdpk]);
push (@parsearray, ['WND', \$wdir]);
push (@parsearray, ['SOL', \$solar]);
push (@parsearray, ['RAI', \$rainmm]);
push (@parsearray, ['DRAI', \$dailyrainmm]);
push (@parsearray, ['WRAI', \$weeklyrainmm]);
push (@parsearray, ['MRAI', \$monthlyrainmm]);
push (@parsearray, ['YRAI', \$yearlyrainmm]);
push (@parsearray, ['DPT', \$dewpoint]);
push (@parsearray, ['TS', \$ts]);

$html = "";

open (IN, "<$dir/template.html");
while (<IN>) {
        $html .= $_;
}
close (IN);
$html = &parse_template_array($html, \@parsearray);

open (OUT, ">$dir/index.html");
print OUT $html;
close OUT;

$html = "";

open (IN, "<$edir/template.html"); 
while (<IN>) {
        $html .= $_;
}
close (IN);
$html = &parse_template_array($html, \@parsearray);

open (OUT, ">$edir/index.html");
print OUT $html;
close OUT;

#
#	wu-data HTML output
#

$pressin = $pressure < 0 ? "N/A" : $pressure;
$dailyrainin = sprintf "%.2f", $dailyrainmm/25.4;
$html = "stationDate=$date
stationTime=$time
windDir=$wdir
wind10Avg=$wspd
windSpeed=$wspdpk
outsideTemp=$temp
outsideHumidity=$humidity
outsideDewPt=$dewpoint
Rain=$rainin
dailyRain=$dailyrainin
barometer=$pressin
indoorTemp=$temp3
tempUnit=C
windUnit=m/s
barUnit=in
rainUnit=in
";

open (OUT, ">$dir/wu-data.html"); 
print OUT $html;
close OUT;
#
#	AWEKAS output
#
$windkmh = 3.6*$wspd;
$dat = `date +%d=%m=%Y`;
$pressmbr = sprintf "%.0f", $pressure * 33.8640;

$txt = "
$temp
$humidity
$pressmbr
$dailyrainmm
$windkmh
$wdir
$time
$dat
$press6hr
";

$txt =~ s/\./,/g;
$txt =~ s/=/./g;

open (OUT, ">$dir/awekas.txt");
print OUT $txt;
close OUT;

#
#	MySQL update
#
$dbh = DBI->connect("DBI:mysql:$db_name:localhost:3306", $db_user, $db_pass);

if ( $DBI::errstr ne "" ) {
    print "Could not connect to MySQL server, Error: $DBI::errstr\n";
    exit;
}

#$ts = `date -u +%s`;
#$ts -= ($ts % 300);

$ts = $uts eq 'U' ? "NULL" : $uts;
$temp = $temp2 eq 'U' ? "NULL" : sprintf "%.1f", $temp2 - 0.57;
#$pressure = $pressure eq 'U' ? "NULL" : sprintf "%.0f", $pressure*33.864;
$pressure = $pressure eq 'U' ? "NULL" : sprintf "%.0f", $pressure * 25.4;
$humidity = "NULL" if $humidity eq 'U';
$wspd = "NULL" if $wspd eq 'U';
$wspdpk = "NULL" if $wspdpk eq 'U';
$wdir = "NULL" if $wdir eq 'U';
$raincnt = "NULL" if $raincnt eq 'U';
$solar = "NULL" if $solar eq 'U';

$cmd = "INSERT INTO meteo_data(ts, temp, pressure, humidity, 
	wind_dir, wind_spd, wind_gust, rain, station_id, solar) 
	VALUES ($ts, $temp, $pressure, $humidity,
	$wdir, $wspd, $wspdpk, $raincnt, '$id', $solar)";

$sth = $dbh->prepare($cmd);
$rv = $sth->execute;

$sth->finish;
$dbh->disconnect;

sub dailyrain {
  my ($rc0, $rc, $fdt, $dd, $ww, $mm, $yy);
  $rc = $_[0];
  $fdt = `date +%d:%V:%m:%y`;
  chomp $fdt;
  ($dd, $ww, $mm, $yy) = split /:/, $fdt;
  
  if (open (IN, "<$RCFILE-d.txt")) {
    $_ = <IN>;
    chomp;
    ($day, $rc0) = split /,/;
    if ( "$day" eq "$dd" ) { # data exists
	close (IN);
        return $rc0;
    } 
  }
# old data, we need to update it. 
  if (open (OUT, ">$RCFILE-d.txt")) {
    print OUT "$dd,$rc\n";
    close (OUT);
    return $rc;
  } else { # could not create file 
    return "U";
  }
}

sub weeklyrain {
  my ($rc0, $rc, $fdt, $dd, $ww, $mm, $yy);
  $rc = $_[0];
  $fdt = `date +%d:%V:%m:%y`;
  chomp $fdt;
  ($dd, $ww, $mm, $yy) = split /:/, $fdt;
  
  if (open (IN, "<$RCFILE-w.txt")) {
    $_ = <IN>;
    chomp;
    ($week, $rc0) = split /,/;
    if ( "$week" eq "$ww" ) { # data exists
	close (IN);
        return $rc0;
    } 
  }
# old data, we need to update it. 
  if (open (OUT, ">$RCFILE-w.txt")) {
    print OUT "$ww,$rc\n";
    close (OUT);
    return $rc;
  } else { # could not create file 
    return "U";
  }
}

sub monthlyrain {
  my ($rc0, $rc, $fdt, $dd, $ww, $mm, $yy);
  $rc = $_[0];
  $fdt = `date +%d:%V:%m:%y`;
  chomp $fdt;
  ($dd, $ww, $mm, $yy) = split /:/, $fdt;
  
  if (open (IN, "<$RCFILE-m.txt")) {
    $_ = <IN>;
    chomp;
    ($month, $rc0) = split /,/;
    if ( "$month" eq "$mm" ) { # data exists
	close (IN);
        return $rc0;
    } 
  }
# old data, we need to update it. 
  if (open (OUT, ">$RCFILE-m.txt")) {
    print OUT "$mm,$rc\n";
    close (OUT);
    return $rc;
  } else { # could not create file 
    return "U";
  }
}

sub yearlyrain {
  my ($rc0, $rc, $fdt, $dd, $ww, $mm, $yy);
  $rc = $_[0];
  $fdt = `date +%d:%V:%m:%y`;
  chomp $fdt;
  ($dd, $ww, $mm, $yy) = split /:/, $fdt;
  
  if (open (IN, "<$RCFILE-y.txt")) {
    $_ = <IN>;
    chomp;
    ($year, $rc0) = split /,/;
    if ( "$year" eq "$yy" ) { # data exists
	close (IN);
        return $rc0;
    } 
  }
# old data, we need to update it. 
  if (open (OUT, ">$RCFILE-y.txt")) {
    print OUT "$yy,$rc\n";
    close (OUT);
    return $rc;
  } else { # could not create file 
    return "U";
  }
}

