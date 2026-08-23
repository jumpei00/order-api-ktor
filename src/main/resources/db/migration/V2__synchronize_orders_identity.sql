SELECT
  setval (
    pg_get_serial_sequence ('orders', 'id'),
    GREATEST (COALESCE(MAX(id), 0) + 1, 1),
    false
  )
FROM
  orders;
