declare namespace cds = "http://marklogic.com/mlcs/cds";
declare variable $URI as xs:string external;

let $colls := xdmp:document-get-collections($URI)
let $data-document := 
    element cds:DataEnvelope
     {
       element cds:UniqueIdentifier { $URI },
       for $coll in $colls
       return element cds:DomainCollection {$coll},
       element cds:SystemTimeStamp { fn:current-dateTime() }, 
       element cds:Data { 
           fn:doc($URI)/element()
      }
    }

return $data-document