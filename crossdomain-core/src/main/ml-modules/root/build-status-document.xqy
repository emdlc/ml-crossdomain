declare namespace cds = "http://marklogic.com/mlcs/cds";

declare variable $collections := ("person", "vehicle", "email-address");
declare function local:buildStatusEnvelope() {
  let $metadata-document := 
    element { 'cds:StatusEnvelope' }
     {
       element cds:SystemTimeStamp { fn:current-dateTime() },
       element cds:TotalCount { xdmp:estimate(fn:doc()) },
       for $coll at $pos in $collections
       let $coll-count := xdmp:estimate(cts:search(fn:doc(), cts:collection-query($coll)))
       return element cds:DomainCollection { attribute count { $coll-count },  $coll }
    }
  return $metadata-document
};
let $status-doc := local:buildStatusEnvelope()
return $status-doc