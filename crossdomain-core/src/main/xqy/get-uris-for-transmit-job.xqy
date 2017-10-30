(: 
This query should be updated to pull documents based on some sort of Date Range Index so it only pulls updated
documents to be transmitted
:)

let $uris := cts:uris( (), ('limit=200'), () )
return $uris[150 to fn:count($uris)]