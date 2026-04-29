select *
from {{ schema }}.orders
where 1 = 1
{% if date_from %}
  and created_at >= {{ date_from | sql_string }}
{% endif %}
{% if date_to %}
  and created_at < {{ date_to | sql_string }}
{% endif %}

{% for table in tables %}
select count(*) as cnt, '{{ table }}' as table_name
from {{ schema }}.{{ table }};
{% endfor %}

