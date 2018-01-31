xquery version "1.0-ml";

declare namespace cds = "http://marklogic.com/mlcs/cds";

(: declare variable $statusTimeStamp as xs:string external; :)
declare variable $statusTimeStamp := xdmp:get-request-field("statusTimeStamp", xs:string(fn:current-dateTime()));

declare function local:get-counts-from-latest-status() {

  let $latest-timestamp := 
    if($statusTimeStamp ne "") then
      xs:dateTime($statusTimeStamp)
    else 
      cts:element-values(xs:QName("cds:SystemTimeStamp"), (), ("limit=1","descending"), cts:collection-query("status"))
  let $latest-uri := cts:uris( (), (), cts:element-range-query(xs:QName("cds:SystemTimeStamp"), "=", $latest-timestamp))
  let $statusDoc :=  fn:doc($latest-uri)
  
  let $header := fn:string-join( ("Collection Name", "Latest Status Count", "Current Database Count"), ",")
  let $total-status-count := fn:string($statusDoc//cds:TotalCount)
  let $total-db-count := fn:string(xdmp:estimate(cts:search(fn:doc(), cts:directory-query("/data/"))))
  let $totals-row := fn:string-join( ("Total Documents", $total-status-count, $total-db-count), ",")
  
  let $domain-rows :=
      (
      for $domain-coll in $statusDoc//cds:DomainCollection
      let $coll-name := fn:string($domain-coll)
      let $coll-count := fn:string($domain-coll/@count)
      let $current-db-coll-count := fn:string(xdmp:estimate(cts:search(fn:doc(), cts:collection-query($coll-name))))
      return fn:string-join( ($coll-name, $coll-count, $current-db-coll-count), ","),
      
      let $coll-name := "Files Added"
      let $imported-file-uris := $statusDoc//filesAdded/file
      let $coll-count := fn:string(fn:count($imported-file-uris))
      let $imported-doc-query := cts:document-query($imported-file-uris)
      let $current-db-coll-count := fn:string(xdmp:estimate(cts:search(fn:doc(), $imported-doc-query) ))
      return fn:string-join( ($coll-name, $coll-count, $current-db-coll-count), ",")
      )
  let $output-xml :=
    element table { attribute border {"1"} ,
      for $row in ($header, $totals-row, $domain-rows)
      let $columns := fn:tokenize($row, ",")
      return element tr {
        for $column in $columns
        return element td { $column }
      }
    }

  let $exports := cts:element-values(xs:QName("cds:SystemTimeStamp"), (), ("limit=10","descending"), cts:collection-query("status"))
  let $output-xml2 :=
    element table { 
      for $value in ($exports)
      let $columns := $value
      return element tr {
        for $column in $columns
        let $link := fn:concat("?statusTimeStamp=", $value)
        return element td { 
          element a { attribute href {$link}, $value }
          }
      }
    }
  
  return (
  (:
      $latest-timestamp,
      $latest-uri,
      $statusDoc,
  :)
      fn:concat(xdmp:quote($output-xml), "<p>Status Timestamps:</p>", xdmp:quote($output-xml2) )
  )
};


declare function local:get-counts() {
    let $output := local:get-counts-from-latest-status() 
    let $value := "<html><head></head><body>" || $output || "</body></html>"
    return $value
};

let $output := local:get-counts()
return (xdmp:set-response-content-type("text/html"), $output)