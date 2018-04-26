(: 
  This query pulls documents updated since the last pull and updates the timestamp for the last pull
  If this is the initial run, the last timestamp will be set to January 1st, 2000.
:)

declare namespace prop="http://marklogic.com/xdmp/property";
declare variable $RUNTIME-URI := "/cds/last-transmit-runtime.xml";

let $lastTransmitRuntime := 
  if(fn:exists(fn:doc($RUNTIME-URI)/lastTransmitRuntime)) then 
      xs:dateTime(fn:doc($RUNTIME-URI)/lastTransmitRuntime/fn:string())
  else
      xs:dateTime("2000-01-01T00:00:00")
    
let $last-mod-query := cts:element-range-query(xs:QName("prop:last-modified"), ">=", $lastTransmitRuntime)
let $uris := cts:uris( (), ("properties"), $last-mod-query)
let $timestamp-ordered-uris :=
    for $uri in $uris
    let $lastModDate := cts:element-values(xs:QName("prop:last-modified"), (), ("properties"), cts:document-query($uri))
    order by $lastModDate
    return $uri

let $doc := element lastTransmitRuntime { fn:current-dateTime() }
let $_ := xdmp:document-insert($RUNTIME-URI, $doc)

return $timestamp-ordered-uris