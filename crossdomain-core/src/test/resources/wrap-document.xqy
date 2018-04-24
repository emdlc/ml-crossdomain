declare namespace cds = "http://marklogic.com/mlcs/cds";
declare variable $URI as xs:string external;

let $coll := xdmp:document-get-collections($URI)[1]  
let $data-document := 
    element cds:DataEnvelope
     {
       element cds:UniqueIdentifier { $URI },
       element cds:DomainCollection { $coll },
       element cds:SystemTimeStamp { fn:current-dateTime() }, 
       element cds:Data { 
           fn:doc($URI)/element()
      }
    }
  
return $data-document