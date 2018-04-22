import requests
import yaml

with open('config.yml', 'r') as f:
    config = yaml.load(f)

host = config['host']
port = config['port']
maze_endpoint = config['maze_endpoint']
parameter_list = config['params']

request_url = 'http://{}:{}/{}'.format(host, port, maze_endpoint)

for i, params in enumerate(parameter_list):
    r = requests.get(request_url, params)
    elapsed_secs = r.elapsed.total_seconds()
    print('Request {} took {}s'.format(i+1, elapsed_secs))
    print(r.url)
