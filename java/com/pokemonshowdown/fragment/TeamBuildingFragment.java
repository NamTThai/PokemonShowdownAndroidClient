package com.pokemonshowdown.fragment;

/**
 * This class is a Fragment loaded in the TeamBuildingActivity
 * This enables the user to add/remove/edit pokemons from the team
 */
public class TeamBuildingFragment /*extends Fragment*/ {
//    public static final String TAG = TeamBuildingFragment.class.getName();
//    public final static String TEAMTAG = "team";
//    private PokemonTeam mPokemonTeam;
//    private PokemonListAdapter mPokemonListAdapter;
//    private Button mFooterButton;
//
//    /* Used for onactivityresult */
//    private int mSelectedPos;
//    private int mSelectedMove;
//
//    public TeamBuildingFragment() {
//
//    }
//
//    public static TeamBuildingFragment newInstance(PokemonTeam team) {
//        TeamBuildingFragment fragment = new TeamBuildingFragment();
//        Bundle bundle = new Bundle();
//        bundle.putSerializable(TEAMTAG, team);
//        fragment.setArguments(bundle);
//        return fragment;
//    }
//
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == Activity.RESULT_OK) {
//            if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_POKEMON) {
//                Pokemon pokemon = new Pokemon(getActivity(), data.getExtras().getString("Search"));
//                if (mSelectedPos != -1) {
//                    mPokemonTeam.replacePokemon(mSelectedPos, pokemon);
//                } else {
//                    mPokemonTeam.addPokemon(pokemon);
//                }
//
//                if (mPokemonTeam.isFull()) {
//                    mFooterButton.setVisibility(View.GONE);
//                } else {
//                    mFooterButton.setVisibility(View.VISIBLE);
//                }
//
//                mPokemonListAdapter.notifyDataSetChanged();
//            } else if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_ITEM) {
//                String item = data.getExtras().getString("Search");
//
//                Pokemon pkmn = mPokemonTeam.getPokemon(mSelectedPos);
//                pkmn.setItem(item);
//
//                mPokemonListAdapter.notifyDataSetChanged();
//            } else if (requestCode == SearchableActivity.REQUEST_CODE_SEARCH_MOVES) {
//                String move = data.getExtras().getString("Search");
//
//                Pokemon pkmn = mPokemonTeam.getPokemon(mSelectedPos);
//                switch (mSelectedMove) {
//                    case 1:
//                        pkmn.setMove1(move);
//                        break;
//                    case 2:
//                        pkmn.setMove2(move);
//                        break;
//                    case 3:
//                        pkmn.setMove3(move);
//                        break;
//                    case 4:
//                        pkmn.setMove4(move);
//                        break;
//                    default:
//                        break;
//                }
//
//                // hiddenpower X : change IV'
//                // furstration : set 0 happiness
//                // return get max happinnes
//
//                if ("frustration".equals(move)) {
//                    pkmn.setHappiness(0);
//                    Toast.makeText(getActivity(), getText(R.string.happiness_min), Toast.LENGTH_SHORT).show();
//                } else if ("return".equals(move)) {
//                    pkmn.setHappiness(255);
//                    Toast.makeText(getActivity(), getText(R.string.happiness_max), Toast.LENGTH_SHORT).show();
//                } else if ("gyroball".equals(move) || "trickroom".equals(move)) {
//                    boolean hasHP = false;
//                    for (int i = 1; i <= 4; i++) {
//                        if (pkmn.getMove(i) != null && (pkmn.getMove(i).contains("hiddenpower"))) {
//                            hasHP = true;
//                            break;
//                        }
//                    }
//
//                    if (hasHP) {
//                        pkmn.setSpdIV(pkmn.getSpdIV() % 4);
//                    } else {
//                        pkmn.setSpdIV(0);
//                    }
//
//                    Toast.makeText(getActivity(), getText(R.string.speediv_modified), Toast.LENGTH_SHORT).show();
//                } else if (move != null && move.contains("hiddenpower")) {
//                    if (move.length() > "hiddenpower".length()) {
//                        boolean needsLowSpeed = false;
//                        for (int i = 1; i <= 4; i++) {
//                            if (pkmn.getMove(i) != null && (pkmn.getMove(i).equals("gyroball") || pkmn.getMove(i).equals("trickroom"))) {
//                                needsLowSpeed = true;
//                                break;
//                            }
//                        }
//                        String hpType = move.substring("hiddenpower".length());
//                        switch (hpType) {
//                            case "bug":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "dark":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "dragon":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "electric":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "fighting":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(30);
//                                break;
//
//                            case "fire":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(30);
//                                break;
//
//                            case "flying":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(30);
//                                break;
//
//                            case "ghost":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "grass":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "ground":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "ice":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "poison":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "psychic":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(30);
//                                break;
//
//                            case "rock":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(30);
//                                break;
//
//                            case "steel":
//                                pkmn.setAtkIV(31);
//                                pkmn.setDefIV(31);
//                                pkmn.setSpAtkIV(31);
//                                pkmn.setSpDefIV(30);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            case "water":
//                                pkmn.setAtkIV(30);
//                                pkmn.setDefIV(30);
//                                pkmn.setSpAtkIV(30);
//                                pkmn.setSpDefIV(31);
//                                pkmn.setSpdIV(31);
//                                break;
//
//                            default:
//                                break;
//                        }
//                        if (needsLowSpeed) {
//                            pkmn.setSpdIV(pkmn.getSpdIV() % 4);
//                        }
//                        Toast.makeText(getActivity(), getText(R.string.ivs_modified), Toast.LENGTH_SHORT).show();
//                    }
//                }
//
//                mPokemonListAdapter.notifyDataSetChanged();
//            }
//        }
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        mPokemonTeam = (PokemonTeam) getArguments().get(TEAMTAG);
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_teambuilding, parent, false);
//        ListView lv = (ListView) view.findViewById(R.id.lisview_pokemonteamlist);
//
//        // add pokemon button
//        mFooterButton = new Button(getActivity().getApplicationContext());
//        mFooterButton.setText(R.string.add_pokemon_button_text);
//        mFooterButton.setTextColor(Color.BLACK);
//        mFooterButton.setBackgroundColor(Color.WHITE);
//        mFooterButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_new, 0, 0, 0);
//        mFooterButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (!mPokemonTeam.isFull()) {
//                    mSelectedPos = -1;
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
//                    intent.putExtra(SearchableActivity.CURRENT_TIER, mPokemonTeam.getTier());
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
//                }
//            }
//        });
//        lv.addFooterView(mFooterButton);
//
//        registerForContextMenu(lv);
//
//        // pokemon list adapter
//        mPokemonListAdapter = new PokemonListAdapter(getActivity().getApplicationContext(), mPokemonTeam.getPokemons());
//        lv.setAdapter(mPokemonListAdapter);
//        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//            public void onItemClick(AdapterView<?> parent, View view,
//                                    int position, long id) {
//                Pokemon pkmn = mPokemonTeam.getPokemon(position);
//                mSelectedPos = position;
//                PokemonFragment fragment = new PokemonFragment();
//                Bundle bundle = new Bundle();
//                bundle.putSerializable(PokemonFragment.POKEMON, pkmn);
//                bundle.putBoolean(PokemonFragment.SEARCH, false);
//                fragment.setArguments(bundle);
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                fragment.show(fragmentManager, PokemonFragment.PTAG);
//            }
//        });
//
//        lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
//            @Override
//            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {
//                return false;
//            }
//        });
//
//        return view;
//    }
//
//    @Override
//    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//        view.setFocusableInTouchMode(true);
//    }
//
//    @Override
//    public void onResume() {
//        super.onResume();
//        mPokemonListAdapter.notifyDataSetChanged();
//        // Notify parent activity that the mPokemonTeam changed (so to reprint in the drawer)
//    }
//
//    @Override
//    public void onCreateContextMenu(ContextMenu menu, View v,
//                                    ContextMenu.ContextMenuInfo menuInfo) {
//        menu.add(Menu.NONE, 1, Menu.NONE, R.string.remove_pokemon);
//        menu.add(Menu.NONE, 2, Menu.NONE, R.string.replace_pokemon);
//    }
//
//    @Override
//    public boolean onContextItemSelected(MenuItem item) {
//        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
//
//        switch (item.getItemId()) {
//            case 1:
//                mPokemonTeam.removePokemon(info.position);
//                mPokemonListAdapter.notifyDataSetChanged();
//                // Notify parent activity that the mPokemonTeam changed (so to reprint in the drawer)
//                if (mPokemonTeam.isFull()) {
//                    mFooterButton.setVisibility(View.GONE);
//                } else {
//                    mFooterButton.setVisibility(View.VISIBLE);
//                }
//                return true;
//
//            case 2:
//                mSelectedPos = info.position;
//                Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
//                startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_POKEMON);
//                return true;
//            default:
//                return super.onContextItemSelected(item);
//        }
//    }
//
//    public void updateList() {
//        mPokemonListAdapter.notifyDataSetChanged();
//    }
//
//    /**
//     * Custom adapter for showing pokemons in a list including
//     * moves, item...
//     */
//    private class PokemonListAdapter extends ArrayAdapter<Pokemon> {
//        public PokemonListAdapter(Context getContext, List<Pokemon> userListData) {
//            super(getContext, 0, userListData);
//        }
//
//        @Override
//        public View getView(final int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = getActivity().getLayoutInflater().inflate(R.layout.listwidget_detailledpokemon, null);
//            }
//
//            final Pokemon pokemon = mPokemonTeam.getPokemon(position);
//
//
//            TextView pokemonNickNameTextView = (TextView) convertView.findViewById(R.id.teambuilder_pokemonNickName);
//            pokemonNickNameTextView.setText(pokemon.getNickName());
//            pokemonNickNameTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    AlertDialog.Builder renameDialog = new AlertDialog.Builder(TeamBuildingFragment.this.getActivity());
//                    renameDialog.setTitle(R.string.rename_pokemon);
//                    final EditText teamNameEditText = new EditText(TeamBuildingFragment.this.getActivity());
//                    teamNameEditText.setText(pokemon.getNickName());
//                    renameDialog.setView(teamNameEditText);
//
//                    renameDialog.setPositiveButton(R.string.dialog_ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            if (teamNameEditText.getText().toString().isEmpty()) {
//                                pokemon.setNickName(pokemon.getName());
//                            } else {
//                                pokemon.setNickName(teamNameEditText.getText().toString());
//                            }
//                            notifyDataSetChanged();
//                            arg0.dismiss();
//                        }
//                    });
//
//                    renameDialog.setNegativeButton(R.string.dialog_cancel, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface arg0, int arg1) {
//                            arg0.dismiss();
//                        }
//                    });
//
//                    renameDialog.show();
//                }
//            });
//
//            ImageView pokemonIconImageView = (ImageView) convertView.findViewById(R.id.teambuilder_pokemonIcon);
//            pokemonIconImageView.setImageDrawable(getResources().getDrawable(pokemon.getFrontSprite()));
//
//
//            TextView itemNameTextView = (TextView) convertView.findViewById(R.id.teambuilder_item);
//            if (pokemon.getItem().isEmpty()) {
//                itemNameTextView.setText(R.string.pokemon_nohelditem);
//                itemNameTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//            } else {
//                String itemString = pokemon.getItem();
//                JSONObject itemJSon = ItemDex.get(getActivity()).getItemJsonObject(itemString);
//                if (itemJSon != null) {
//                    try {
//                        String itemName = itemJSon.getString("name");
//                        itemNameTextView.setText(itemName);
//                        int itemDrawable = ItemDex.getItemIcon(TeamBuildingFragment.this.getActivity(), pokemon.getItem());
//                        if (itemDrawable != 0) {
//                            itemNameTextView.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(itemDrawable), null, null, null);
//                        }
//                    } catch (JSONException e) {
//                        itemNameTextView.setText(R.string.pokemon_nohelditem);
//                        itemNameTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//                    }
//                } else {
//                    //wrong item data
//                    itemNameTextView.setText(R.string.pokemon_nohelditem);
//                    itemNameTextView.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
//                    pokemon.setItem("");
//                }
//
//            }
//            itemNameTextView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
//                    mSelectedPos = position;
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_ITEM);
//                }
//            });
//
//            if (!pokemon.getMove1().equals("")) {
//                ImageView move1TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move1_type);
//                move1TypeImageView.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove1()));
//                JSONObject move1Object = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove1());
//                if (move1Object != null) {
//                    TextView move1PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move1_pp);
//                    TextView move1NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move1_name);
//                    try {
//                        String pp = String.valueOf(move1Object.getInt("pp"));
//                        pp = MoveDex.getMaxPP(pp);
//                        move1PpTextView.setText(pp + "/" + pp);
//                        move1NameTextView.setText(move1Object.getString("name"));
//                    } catch (JSONException e) {
//                        pokemon.setMove1("");
//                        Log.e(TAG, "", e);
//                    }
//                }
//            } else {
//                // in case it's an old view
//                TextView move1PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move1_pp);
//                move1PpTextView.setText("");
//                ImageView move1TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move1_type);
//                move1TypeImageView.setImageDrawable(null);
//                TextView move1NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move1_name);
//                move1NameTextView.setText("");
//            }
//
//            if (!pokemon.getMove2().equals("")) {
//                ImageView move2TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move2_type);
//                move2TypeImageView.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove2()));
//                JSONObject move2Object = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove2());
//                if (move2Object != null) {
//                    TextView move2PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move2_pp);
//                    TextView move2NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move2_name);
//                    try {
//                        String pp = String.valueOf(move2Object.getInt("pp"));
//                        pp = MoveDex.getMaxPP(pp);
//                        move2PpTextView.setText(pp + "/" + pp);
//                        move2NameTextView.setText(move2Object.getString("name"));
//                    } catch (JSONException e) {
//                        pokemon.setMove2("");
//                        Log.e(TAG, "", e);
//                    }
//                }
//            } else {
//                // in case it's an old view
//                TextView move2PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move2_pp);
//                move2PpTextView.setText("");
//                ImageView move2TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move2_type);
//                move2TypeImageView.setImageDrawable(null);
//                TextView move2NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move2_name);
//                move2NameTextView.setText("");
//            }
//
//
//            if (!pokemon.getMove3().equals("")) {
//                ImageView move3TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move3_type);
//                move3TypeImageView.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove3()));
//                JSONObject move3Object = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove3());
//                if (move3Object != null) {
//                    TextView move3PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move3_pp);
//                    TextView move3NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move3_name);
//                    try {
//                        String pp = String.valueOf(move3Object.getInt("pp"));
//                        pp = MoveDex.getMaxPP(pp);
//                        move3PpTextView.setText(pp + "/" + pp);
//                        move3NameTextView.setText(move3Object.getString("name"));
//                    } catch (JSONException e) {
//                        pokemon.setMove3("");
//                        Log.e(TAG, "", e);
//                    }
//                }
//            } else {
//                // in case it's an old view
//                TextView move3PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move3_pp);
//                move3PpTextView.setText("");
//                ImageView move3TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move3_type);
//                move3TypeImageView.setImageDrawable(null);
//                TextView move3NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move3_name);
//                move3NameTextView.setText("");
//            }
//
//            if (!pokemon.getMove4().equals("")) {
//                ImageView move4TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move4_type);
//                move4TypeImageView.setImageResource(MoveDex.getMoveTypeIcon(getActivity(), pokemon.getMove4()));
//                JSONObject move4Object = MoveDex.get(getActivity()).getMoveJsonObject(pokemon.getMove4());
//                if (move4Object != null) {
//                    TextView move4PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move4_pp);
//                    TextView move4NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move4_name);
//                    try {
//                        String pp = String.valueOf(move4Object.getInt("pp"));
//                        pp = MoveDex.getMaxPP(pp);
//                        move4PpTextView.setText(pp + "/" + pp);
//                        move4NameTextView.setText(move4Object.getString("name"));
//                    } catch (JSONException e) {
//                        pokemon.setMove4("");
//                        Log.e(TAG, "", e);
//                    }
//                }
//            } else {
//                // in case it's an old view
//                TextView move4PpTextView = (TextView) convertView.findViewById(R.id.teambuilder_move4_pp);
//                move4PpTextView.setText("");
//                ImageView move4TypeImageView = (ImageView) convertView.findViewById(R.id.teambuilder_move4_type);
//                move4TypeImageView.setImageDrawable(null);
//                TextView move4NameTextView = (TextView) convertView.findViewById(R.id.teambuilder_move4_name);
//                move4NameTextView.setText("");
//            }
//
//
//            RelativeLayout move1 = (RelativeLayout) convertView.findViewById(R.id.teambuilder_move1);
//            move1.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                    intent.putExtra(SearchableActivity.POKEMON_LEARNSET, pokemon.getName());
//                    mSelectedPos = position;
//                    mSelectedMove = 1;
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                }
//            });
//
//            RelativeLayout move2 = (RelativeLayout) convertView.findViewById(R.id.teambuilder_move2);
//            move2.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                    intent.putExtra(SearchableActivity.POKEMON_LEARNSET, pokemon.getName());
//                    mSelectedPos = position;
//                    mSelectedMove = 2;
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                }
//            });
//
//
//            RelativeLayout move3 = (RelativeLayout) convertView.findViewById(R.id.teambuilder_move3);
//            move3.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                    intent.putExtra(SearchableActivity.POKEMON_LEARNSET, pokemon.getName());
//                    mSelectedPos = position;
//                    mSelectedMove = 3;
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                }
//            });
//
//
//            RelativeLayout move4 = (RelativeLayout) convertView.findViewById(R.id.teambuilder_move4);
//            move4.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    Intent intent = new Intent(getActivity().getApplicationContext(), SearchableActivity.class);
//                    intent.putExtra(SearchableActivity.SEARCH_TYPE, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                    intent.putExtra(SearchableActivity.POKEMON_LEARNSET, pokemon.getName());
//                    mSelectedPos = position;
//                    mSelectedMove = 4;
//                    startActivityForResult(intent, SearchableActivity.REQUEST_CODE_SEARCH_MOVES);
//                }
//            });
//
//            return convertView;
//        }
//    }
}
